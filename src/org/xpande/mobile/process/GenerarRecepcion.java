package org.xpande.mobile.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.acct.Doc;
import org.compiere.model.*;
import org.compiere.process.DocAction;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.xpande.comercial.model.MZComercialConfig;
import org.xpande.comercial.utils.ComercialUtils;
import org.xpande.core.model.MZProductoUPC;
import org.xpande.core.model.MZSocioListaPrecio;
import org.xpande.mobile.model.MZMBInOut;
import org.xpande.mobile.model.MZMBInOutFact;
import org.xpande.mobile.model.MZMBInOutLine;
import org.xpande.mobile.model.MZMBInOutUPC;
import org.xpande.retail.model.*;
import org.xpande.retail.model.MProductPricing;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * Proceso que genera recepciones de productos en Adempiere, a partir de datos generados desde un dispositivo móvil.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 12/15/17.
 */
public class GenerarRecepcion extends SvrProcess {

    @Override
    protected void prepare() {

    }

    @Override
    protected String doIt() throws Exception {

        try{

            // Instancio configurador comercial
            MZComercialConfig comercialConfig = MZComercialConfig.getDefault(getCtx(), get_TrxName());

            List<MZMBInOut> mzmbInOutList = MZMBInOut.getNotExecuted(getCtx(), get_TrxName());
            for (MZMBInOut mzmbInOut: mzmbInOutList){

                // Bloqueo cabezal de recepcion para que no sea considerado en otro proceso
                mzmbInOut.setProcessing(true);
                mzmbInOut.saveEx();

                MWarehouse[] warehouses = MWarehouse.getForOrg(getCtx(), mzmbInOut.getAD_Org_ID());
                if (warehouses.length <= 0){
                    continue;
                }
                MWarehouse warehouse = warehouses[0];
                MLocator locator = MLocator.getDefault(warehouse);

                MBPartner partner = new MBPartner(getCtx(), mzmbInOut.getC_BPartner_ID(), null);
                MBPartnerLocation[] locations = partner.getLocations(true);

                if (locations.length <= 0){
                    mzmbInOut.setErrorMsg("El Socio de Negocio no tiene Localizaciones Definidas.");
                    mzmbInOut.setProcessing(false);
                    mzmbInOut.saveEx();
                    continue;
                }

                MBPartnerLocation location = locations[0];

                // Genero cabezal de Recepcion en Adempiere
                MInOut inOut = new MInOut(getCtx(), 0, get_TrxName());
                inOut.set_ValueOfColumn("AD_Client_ID", mzmbInOut.getAD_Client_ID());
                inOut.setAD_Org_ID(mzmbInOut.getAD_Org_ID());
                inOut.setM_Warehouse_ID(warehouse.get_ID());
                inOut.setIsSOTrx(mzmbInOut.isSOTrx());
                inOut.setC_DocType_ID(comercialConfig.getDefaultDocMMR_ID());
                inOut.setDescription(mzmbInOut.getDescription());
                inOut.setMovementType(mzmbInOut.getMovementType());
                inOut.setMovementDate(mzmbInOut.getMovementDate());
                inOut.setC_BPartner_ID(partner.get_ID());
                inOut.setC_BPartner_Location_ID(location.get_ID());
                inOut.setDeliveryRule(X_M_InOut.DELIVERYRULE_Availability);
                inOut.setFreightCostRule(X_M_InOut.FREIGHTCOSTRULE_FreightIncluded);
                inOut.setDeliveryViaRule(X_M_InOut.DELIVERYVIARULE_Pickup);
                inOut.setPriorityRule(X_M_InOut.PRIORITYRULE_Medium);
                inOut.set_ValueOfColumn("Z_MB_InOut_ID", mzmbInOut.get_ID());
                inOut.saveEx();

                HashMap<String, Integer> hashFacturasAsociadas = new HashMap<String, Integer>();

                // Genera lineas de facturas asociadas en recepcion
                List<MZMBInOutFact> mzmbInOutFactList = mzmbInOut.getFacturasAsociadas();
                for (MZMBInOutFact mzmbInOutFact: mzmbInOutFactList){

                    MZRecepcionProdFact recepcionProdFact = new MZRecepcionProdFact(getCtx(), 0, get_TrxName());
                    recepcionProdFact.set_ValueOfColumn("AD_Client_ID", inOut.getAD_Client_ID());
                    recepcionProdFact.setAD_Org_ID(inOut.getAD_Org_ID());
                    recepcionProdFact.setM_InOut_ID(inOut.get_ID());
                    recepcionProdFact.setC_Currency_ID(mzmbInOutFact.getC_Currency_ID());
                    recepcionProdFact.setDateDoc(mzmbInOutFact.getDateInvoiced());

                    String serieDoc = mzmbInOutFact.getDocumentSerie();
                    if (serieDoc != null){
                        serieDoc = serieDoc.trim().toUpperCase();
                        serieDoc = serieDoc.replace(" ","");
                    }

                    recepcionProdFact.setDocumentSerie(serieDoc);
                    recepcionProdFact.setManualDocumentNo(mzmbInOutFact.getDocumentNoRef());
                    recepcionProdFact.saveEx();

                    hashFacturasAsociadas.put(recepcionProdFact.getManualDocumentNo(), recepcionProdFact.get_ID());

                }

                int lineNo = 0;

                // Recorro codigos de barra recibidos en esta rececpión móvil y asocio aquellos que son nuevos a sus respectivos productos.
                List<MZMBInOutUPC> inOutUPCList = mzmbInOut.getUPCs();
                for (MZMBInOutUPC inOutUPC: inOutUPCList){
                    MZProductoUPC productoUPC = MZProductoUPC.getByUPC(this.getCtx(), inOutUPC.getUPC().trim(), this.get_TrxName());
                    if ((productoUPC == null) || (productoUPC.get_ID() <=0)){
                        productoUPC = new MZProductoUPC(getCtx(), 0, get_TrxName());
                        productoUPC.setAD_Org_ID(0);
                        productoUPC.setM_Product_ID(inOutUPC.getM_Product_ID());
                        productoUPC.setUPC(inOutUPC.getUPC().trim());
                        productoUPC.saveEx();
                    }
                }

                // Genero lineas de recepcion en Adempiere
                List<MZMBInOutLine> mzmbInOutLineList = mzmbInOut.getLines();
                for (MZMBInOutLine mzmbInOutLine: mzmbInOutLineList){

                    lineNo = lineNo + 10;

                    MProduct product = new MProduct(getCtx(), mzmbInOutLine.getM_Product_ID(), null);

                    MInOutLine inOutLine = new MInOutLine(inOut);
                    inOutLine.setLine(lineNo);
                    inOutLine.setM_Locator_ID(locator.get_ID());
                    inOutLine.setM_Product_ID(mzmbInOutLine.getM_Product_ID());
                    inOutLine.setC_UOM_ID(product.getC_UOM_ID());
                    inOutLine.setMovementQty(mzmbInOutLine.getMovementQty());
                    inOutLine.setQtyEntered(mzmbInOutLine.getMovementQty());

                    if (hashFacturasAsociadas.get(mzmbInOutLine.getDocumentNoRef()) == null){
                        mzmbInOut.setErrorMsg("Falta indicar numero de factura recibida.");
                        mzmbInOut.setProcessing(false);
                        mzmbInOut.saveEx();
                        continue;
                    }

                    inOutLine.set_ValueOfColumn("Z_RecepcionProdFact_ID", hashFacturasAsociadas.get(mzmbInOutLine.getDocumentNoRef()).intValue());
                    inOutLine.set_ValueOfColumn("QtyEnteredInvoice", mzmbInOutLine.getQtyInvoiced());

                    // Codigo de barras
                    if (mzmbInOutLine.getUPC() != null){
                        if (!mzmbInOutLine.getUPC().trim().equalsIgnoreCase("")){
                            inOutLine.set_ValueOfColumn("UPC", mzmbInOutLine.getUPC().trim());

                            // Hago nueva asociación de codigo de barras - producto en caso de ser necesario.
                            MZProductoUPC productoUPC = MZProductoUPC.getByUPC(getCtx(), mzmbInOutLine.getUPC().trim(), get_TrxName());
                            if ((productoUPC == null) || (productoUPC.get_ID() <= 0)){
                                productoUPC = new MZProductoUPC(getCtx(), 0, get_TrxName());
                                productoUPC.set_ValueOfColumn("AD_Client_ID", mzmbInOut.getAD_Client_ID());
                                productoUPC.setM_Product_ID(inOutLine.getM_Product_ID());
                                productoUPC.setUPC(mzmbInOutLine.getUPC().trim());
                                productoUPC.saveEx();
                            }
                        }
                    }

                    // Codigo de producto del proveedor
                    MZProductoSocio productoSocio = MZProductoSocio.getByBPartnerProduct(getCtx(), inOut.getC_BPartner_ID(), inOutLine.getM_Product_ID(), null);
                    if ((productoSocio != null) && (productoSocio.get_ID() > 0)){
                        if (productoSocio.getVendorProductNo() != null){
                            inOutLine.set_ValueOfColumn("VendorProductNo", productoSocio.getVendorProductNo());
                        }
                    }

                    inOutLine.saveEx();

                }

                // Completo recepcion en Adempiere y genero facturas en borrador, si asi esta parametrizado en la ventana de Configuración Comercial
                if (comercialConfig.isCompletaRecepcion()){
                    if (!inOut.processIt(DocAction.ACTION_Complete)){
                        mzmbInOut.setErrorMsg("No se pudo Completar Recepcion " + inOut.getDocumentNo() + " : " + inOut.getProcessMsg());
                        mzmbInOut.setProcessing(false);
                        mzmbInOut.saveEx();

                        inOut.deleteEx(true);

                        continue;
                    }

                    inOut.saveEx();

                    // Genero facturas en borrador para asociadas a esta recepción
                    String message = this.generarFacturas(inOut, comercialConfig.getDefaultDocAPI_ID());

                    if (message != null){
                        mzmbInOut.setErrorMsg("No se pudo Generar Facturas desde Recepcion " + inOut.getDocumentNo() + " : " + message);
                        mzmbInOut.setProcessing(false);
                        mzmbInOut.setIsExecuted(true); // Esto para que no siga generando recepción
                        mzmbInOut.setM_InOut_ID(inOut.get_ID());
                        mzmbInOut.saveEx();
                        continue;
                    }
                }

                // Marco recepción móvil como procesada
                mzmbInOut.setIsExecuted(true);
                mzmbInOut.setProcessing(false);
                mzmbInOut.setM_InOut_ID(inOut.get_ID());
                mzmbInOut.saveEx();
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return "OK";
    }


    /***
     * Generar facturas en borrador a partir de la recepcion y el datos de facturas asociadas a la misma.
     * Xpande. Created by Gabriel Vila on 12/21/17.
     * @param mInOut
     * @return
     */
    public String generarFacturas(MInOut mInOut, int cDocTypeID){

        String message = null;
        boolean tieneConstancia = false;
        String action = "";

        try{

            // Configuración de Retail
            MZRetailConfig retailConfig = MZRetailConfig.getDefault(getCtx(), get_TrxName());

            // Obtengo documento a utilizar para generar facturas del proveedor recibidas
            MDocType docType = new MDocType(getCtx(), cDocTypeID, null);
            if ((docType == null) || (docType.get_ID() <= 0)){
                return "No se pudo obtener Documento de Factura a considerar";
            }

            // Instancio modelos necesarios
            MBPartner bp = (MBPartner)mInOut.getC_BPartner();

            Timestamp fechaHoy = TimeUtil.trunc(new Timestamp(System.currentTimeMillis()), TimeUtil.TRUNC_DAY);

            // Obtengo y recorro modelos de facturas recibidas
            List<MZRecepcionProdFact> recepcionProdFacts = MZRecepcionProdFact.getByInOut(getCtx(), mInOut.get_ID(), get_TrxName());
            for (MZRecepcionProdFact recepcionProdFact: recepcionProdFacts){

                // Verifico si no existe un comprobante con el mismo: numero, tipo de documento y socio de negocio.
                // En caso de existir, no genero de nuevo este comprobante.
                MInvoice invoice = ComercialUtils.getInvoiceByDocPartner(getCtx(), mInOut.getAD_Org_ID(), docType.get_ID(),
                        recepcionProdFact.getDocumentSerie(), recepcionProdFact.getManualDocumentNo(), mInOut.getC_BPartner_ID(), get_TrxName());
                if ((invoice != null) && (invoice.get_ID() > 0)){
                    continue;
                }

                Timestamp dateInvoiced = TimeUtil.trunc(recepcionProdFact.getDateDoc(), TimeUtil.TRUNC_DAY);

                // Seteo cabezal de nueva factura
                invoice = new MInvoice(mInOut, dateInvoiced);
                invoice.setDateInvoiced(dateInvoiced);
                invoice.setDateAcct(fechaHoy);
                invoice.setC_DocTypeTarget_ID(docType.get_ID());
                invoice.setC_DocType_ID(docType.get_ID());
                invoice.set_ValueOfColumn("DocumentSerie", recepcionProdFact.getDocumentSerie());
                invoice.setDocumentNo(recepcionProdFact.getManualDocumentNo());
                invoice.setC_BPartner_ID(mInOut.getC_BPartner_ID());
                invoice.setC_BPartner_Location_ID(mInOut.getC_BPartner_Location_ID());
                invoice.setC_Currency_ID(recepcionProdFact.getC_Currency_ID());
                invoice.setAD_Org_ID(mInOut.getAD_Org_ID());
                invoice.set_ValueOfColumn("AmtRounding", Env.ZERO);

                if (bp.getPaymentRulePO() != null){
                    invoice.setPaymentRule(bp.getPaymentRulePO());
                }
                if (bp.getPO_PaymentTerm_ID() > 0){
                    invoice.setC_PaymentTerm_ID(bp.getPO_PaymentTerm_ID());
                }

                // Seteo lista de precios de compra del proveedor segun moneda
                MZSocioListaPrecio socioListaPrecio = MZSocioListaPrecio.getByPartnerCurrency(getCtx(), bp.get_ID(), recepcionProdFact.getC_Currency_ID(), get_TrxName());
                if ((socioListaPrecio == null) || (socioListaPrecio.get_ID() <= 0)){
                    MCurrency currency = (MCurrency) recepcionProdFact.getC_Currency();
                    return "@Error@ " + "No se pudo obtener Lista de Precios de Compra para este Socio de Negocio en Moneda : " + currency.getISO_Code();
                }
                invoice.setM_PriceList_ID(socioListaPrecio.getM_PriceList_ID());


                MPriceList priceList = (MPriceList)socioListaPrecio.getM_PriceList();

                // Seteo impuestos incluidos segun lista de precios
                invoice.setIsTaxIncluded(priceList.isTaxIncluded());
                invoice.set_ValueOfColumn("SubDocBaseType", "RET");
                invoice.saveEx();

                // Dejo asociada la invoice creada con la relación de factura - inout
                action = " update z_recepcionprodfact set c_invoice_id =" + invoice.get_ID() +
                        " where z_recepcionprodfact_id =" + recepcionProdFact.get_ID();
                DB.executeUpdateEx(action, get_TrxName());

                // Instancio cabezal de remito por diferencia de cantidad para esta Recepción-Factura, si luego no tiene monto, lo elimino.
                BigDecimal totalAmtRemito = Env.ZERO;
                MZRemitoDifInv remitoDif = null;

                if (retailConfig.getDefDocRemDifCant_ID() <= 0){
                    throw new AdempiereException("No esta definido el Documento para Remito por Diferencia de Cantidad (RDC) en la Configuración de Retail.");
                }

                MDocType docRemito = new MDocType(getCtx(), retailConfig.getDefDocRemDifCant_ID(), null);
                if ((docRemito == null) || (docRemito.get_ID() <= 0)){
                    throw new AdempiereException("No esta definido el Documento para Remito por Diferencia de Cantidad (RDC)");
                }

                remitoDif = new MZRemitoDifInv(getCtx(), 0, get_TrxName());
                remitoDif.setC_BPartner_ID(invoice.getC_BPartner_ID());
                remitoDif.setC_Currency_ID(invoice.getC_Currency_ID());
                remitoDif.setC_DocType_ID(docRemito.get_ID());
                remitoDif.setC_Invoice_ID(invoice.get_ID());
                remitoDif.setM_InOut_ID(mInOut.get_ID());
                remitoDif.setDateDoc(mInOut.getMovementDate());
                remitoDif.setAD_Org_ID(invoice.getAD_Org_ID());
                remitoDif.setTotalAmt(Env.ZERO);

                // Seteo lineas de nueva factura según lineas de recepcion
                List<MInOutLine> inOutLines = recepcionProdFact.getInOutLines();
                for (MInOutLine inOutLine: inOutLines){

                    MInvoiceLine invLine = new MInvoiceLine(invoice);

                    invLine.setC_Invoice_ID(invoice.get_ID());
                    invLine.setM_Product_ID(inOutLine.getM_Product_ID());
                    invLine.setC_UOM_ID(inOutLine.getC_UOM_ID());

                    if (inOutLine.get_Value("UPC") != null){
                        invLine.set_ValueOfColumn("UPC", inOutLine.get_Value("UPC"));
                    }
                    if (inOutLine.get_Value("VendorProductNo") != null){
                        invLine.set_ValueOfColumn("VendorProductNo", inOutLine.get_Value("VendorProductNo"));
                    }


                    if (inOutLine.get_Value("QtyEnteredInvoice") != null){
                        invLine.setQtyEntered((BigDecimal) inOutLine.get_Value("QtyEnteredInvoice"));
                        invLine.setQtyInvoiced(invLine.getQtyEntered());
                    }
                    else{
                        invLine.setQtyEntered(Env.ZERO);
                        invLine.setQtyInvoiced(Env.ZERO);
                    }

                    // Impuesto del producto (primero impuesto especial de compra, y si no tiene, entonces el impuesto normal
                    MProduct prod = (MProduct)inOutLine.getM_Product();

                    // Seteos de tasa de impuesto segun condiciones.
                    // Si el socio de negocio es literal E, entonces todos sus productos deben ir con la tasa de impuesto para Literal E
                    boolean esLiteralE = false;
                    MBPartner partner = (MBPartner) invoice.getC_BPartner();
                    if (partner.get_ValueAsBoolean("LiteralE")){
                        esLiteralE = true;
                        // Obtengo ID de tasa de impuesto para Literal E desde coniguración comercial
                        MZComercialConfig comercialConfig = MZComercialConfig.getDefault(getCtx(), get_TrxName());
                        if (comercialConfig.getLiteralE_Tax_ID() > 0){
                            invLine.setC_Tax_ID(comercialConfig.getLiteralE_Tax_ID());
                        }
                    }
                    // Si no es Literal E, para invoices compra/venta en Retail, puede suceder que el producto tenga un impuesto especial de compra/venta.
                    if (!esLiteralE){
                        // Impuesto del producto (primero impuesto especial de compra, y si no tiene, entonces el impuesto normal
                        if (prod.get_ValueAsInt("C_TaxCategory_ID_2") > 0){
                            MTaxCategory taxCat = new MTaxCategory(getCtx(), prod.get_ValueAsInt("C_TaxCategory_ID_2"), null);
                            MTax tax = taxCat.getDefaultTax();
                            if (tax != null){
                                if (tax.get_ID() > 0){
                                    invLine.setC_Tax_ID(tax.get_ID());
                                }
                            }
                        }
                        else{
                            if (prod.getC_TaxCategory_ID() > 0){
                                MTaxCategory taxCat = (MTaxCategory)prod.getC_TaxCategory();
                                MTax tax = taxCat.getDefaultTax();
                                if (tax != null){
                                    if (tax.get_ID() > 0){
                                        invLine.setC_Tax_ID(tax.get_ID());
                                    }
                                }
                            }
                        }
                    }

                    // Precios
                    org.xpande.retail.model.MProductPricing productPricing = this.getProductPricing(invLine, invoice);
                    if (productPricing == null){
                        throw new AdempiereException("No se pudo calcular precios y montos para el producto : " + prod.getValue() + " - " + prod.getName());
                    }
                    invLine.setPriceActual(productPricing.getPriceStd());
                    invLine.setPriceList(productPricing.getPriceList());
                    invLine.setPriceLimit(productPricing.getPriceLimit());
                    invLine.setPriceEntered(invLine.getPriceActual());

                    if (productPricing.isCostoHistorico()){
                        invLine.set_ValueOfColumn("PricePO", productPricing.getPricePO());
                        invLine.set_ValueOfColumn("PricePONoDto", productPricing.getPricePO());
                    }
                    else{
                        invLine.set_ValueOfColumn("PricePO", invLine.getPriceEntered());
                        invLine.set_ValueOfColumn("PricePONoDto", invLine.getPriceEntered());
                    }

                    invLine.setLineNetAmt();

                    if (inOutLine.get_ValueAsString("UPC") != null){
                        invLine.set_ValueOfColumn("UPC", inOutLine.get_ValueAsString("UPC"));
                    }
                    if (inOutLine.get_ValueAsString("VendorProductNo") != null){
                        invLine.set_ValueOfColumn("VendorProductNo", inOutLine.get_ValueAsString("VendorProductNo"));
                    }

                    invLine.setM_InOutLine_ID(inOutLine.get_ID());
                    invLine.saveEx();

                    // Proceso remito por diferencia de cantidad
                    BigDecimal amtRemitoLin = remitoDif.setRemitoDiferencia(invoice, invLine, 2, retailConfig.getToleraRemDifLin(), true);
                    if (amtRemitoLin != null){
                        totalAmtRemito = totalAmtRemito.add(amtRemitoLin);
                    }
                }

                // Si tengo remito por diferencia de cantidades
                if (totalAmtRemito.compareTo(Env.ZERO) > 0){

                    boolean generarRemito = true;

                    // Verifico total del remito contra tolerancia sobre total de la configuración de retail
                    if (retailConfig.getToleraRemDifTot() != null){
                        if (totalAmtRemito.compareTo(retailConfig.getToleraRemDifTot()) < 0){
                            generarRemito = false;
                        }
                    }

                    if (generarRemito){
                        remitoDif.setTotalAmt(totalAmtRemito);
                        if (!remitoDif.processIt(DocAction.ACTION_Complete)){
                            message = remitoDif.getProcessMsg();
                            if (message == null){
                                message = "Error al completar documento de Remito por Diferencia de Cantidad (número: " + remitoDif.getDocumentNo() + " )";
                            }
                            return message;
                        }
                        remitoDif.saveEx();
                        tieneConstancia = true;

                    }
                    else{
                        remitoDif.deleteEx(true);
                    }
                }
            }

            if (tieneConstancia){
                mInOut.set_ValueOfColumn("TieneConstancia", true);
                mInOut.saveEx();
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }


    /**
     * 	Get and calculate Product Pricing
     *	@param invoiceLine
     *  @param invoice
     *	@return product pricing
     */
    private org.xpande.retail.model.MProductPricing getProductPricing (MInvoiceLine invoiceLine, MInvoice invoice)
    {
        org.xpande.retail.model.MProductPricing productPricing = null;

        try{
            productPricing = new MProductPricing(invoiceLine.getM_Product_ID(), invoice.getC_BPartner_ID(), invoice.getAD_Org_ID(),
                    invoice.getDateInvoiced(), invoiceLine.getQtyEntered(), false, get_TrxName());
            productPricing.setM_PriceList_ID(invoice.getM_PriceList_ID());
            productPricing.setPriceDate(invoice.getDateInvoiced());

            productPricing.calculatePrice();

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        return productPricing;
    }

}
