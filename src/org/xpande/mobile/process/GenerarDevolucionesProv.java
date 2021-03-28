package org.xpande.mobile.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.process.DocAction;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.xpande.comercial.model.MZComercialConfig;
import org.xpande.comercial.model.MZMotivoDevol;
import org.xpande.comercial.model.MZOrdenDevolucion;
import org.xpande.comercial.model.MZOrdenDevolucionLin;
import org.xpande.core.model.MZProductoUPC;
import org.xpande.core.utils.CurrencyUtils;
import org.xpande.mobile.model.MZMBOrdenDevol;
import org.xpande.mobile.model.MZMBOrdenDevolLine;
import org.xpande.retail.model.MZProductoSocio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

/**
 * Proceso para generar devoluciones a proveedores en ADempiere a partir de ordenes de devoluciones móviles.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/6/18.
 */
public class GenerarDevolucionesProv extends SvrProcess {

    @Override
    protected void prepare() {

    }

    @Override
    protected String doIt() throws Exception {

        try{

            Timestamp fechaHoy = TimeUtil.trunc(new Timestamp(System.currentTimeMillis()), TimeUtil.TRUNC_DAY);

            // Instancio configurador comercial
            MZComercialConfig comercialConfig = MZComercialConfig.getDefault(getCtx(), get_TrxName());

            // Documento configurado para Devoluciones a proveedores
            MDocType doc = new MDocType(getCtx(), comercialConfig.getDefaultDocMMS_ID(), null);
            if ((doc == null) || (doc.get_ID() <= 0)){
                throw new AdempiereException("Falta parametrizar documento para Devolución a Proveedor en Configuración Comercial.");
            }

            // Obtengo y recorro lista de ordenes de devolución móviles que aún no fueron procesadas.
            List<MZMBOrdenDevol> ordenDevolList = MZMBOrdenDevol.getNotExecuted(getCtx(), get_TrxName());
            for (MZMBOrdenDevol mzmbOrdenDevol: ordenDevolList){

                // Bloqueo cabezal para que no sea considerado en otro proceso
                mzmbOrdenDevol.setProcessing(true);
                mzmbOrdenDevol.saveEx();

                MClient client = MClient.get(getCtx(), mzmbOrdenDevol.getAD_Client_ID());
                MAcctSchema schema = client.getAcctSchema();

                MWarehouse warehouse = new MWarehouse(getCtx(), mzmbOrdenDevol.getM_Warehouse_ID(), null);
                MLocator locator = MLocator.getDefault(warehouse);

                MBPartner partner = new MBPartner(getCtx(), mzmbOrdenDevol.getC_BPartner_ID(), null);
                MBPartnerLocation[] locations = partner.getLocations(true);
                if (locations.length <= 0){
                    throw new AdempiereException("El Socio de Negocio no tiene Localización : " + partner.getName());
                }

                // Genero cabezal de Devolución en Adempiere
                MInOut inOut = new MInOut(getCtx(), 0, get_TrxName());
                inOut.set_ValueOfColumn("AD_Client_ID", mzmbOrdenDevol.getAD_Client_ID());
                inOut.setAD_Org_ID(mzmbOrdenDevol.getAD_Org_ID());
                inOut.setC_BPartner_ID(mzmbOrdenDevol.getC_BPartner_ID());
                inOut.setC_BPartner_Location_ID(locations[0].get_ID());
                inOut.set_ValueOfColumn("TaxID", partner.getTaxID());
                inOut.setC_DocType_ID(doc.get_ID());
                inOut.setMovementDate(fechaHoy);
                inOut.setDateAcct(fechaHoy);
                inOut.setDescription(mzmbOrdenDevol.getDescription());
                inOut.setM_Warehouse_ID(warehouse.get_ID());
                inOut.setMovementType(mzmbOrdenDevol.getMovementType());
                inOut.set_ValueOfColumn("C_Currency_ID", schema.getC_Currency_ID());
                inOut.setIsSOTrx(false);
                inOut.setDeliveryRule(X_M_InOut.DELIVERYRULE_Availability);
                inOut.setFreightCostRule(X_M_InOut.FREIGHTCOSTRULE_FreightIncluded);
                inOut.setDeliveryViaRule(X_M_InOut.DELIVERYVIARULE_Pickup);
                inOut.saveEx();

                // Genero lineas para esta devolución en Adempiere
                BigDecimal totalAmt = Env.ZERO;

                List<MZMBOrdenDevolLine> ordenDevolLineList = mzmbOrdenDevol.getLines();
                for (MZMBOrdenDevolLine mzmbOrdenDevolLine: ordenDevolLineList){

                    MProduct product = new MProduct(getCtx(), mzmbOrdenDevolLine.getM_Product_ID(), null);

                    BigDecimal priceInvoiced = Env.ZERO;
                    Timestamp dateInvoiced = null;
                    int cCurrencyID = 0;
                    String vendorProductCode = null, documentNoRef = null;

                    // Instancio modelo de producto-socio para obtener datos de ultima factura
                    MZProductoSocio productoSocio = MZProductoSocio.getByBPartnerProduct(getCtx(), inOut.getC_BPartner_ID(), product.get_ID(), null);

                    // Si no tengo modelo para este socio de negocio de la ultima factura
                    if ((productoSocio == null) || (productoSocio.get_ID() <= 0)){
                        productoSocio = MZProductoSocio.getByLastInvoice(getCtx(), product.get_ID(), null);
                    }
                    else{

                        vendorProductCode = productoSocio.getVendorProductNo();

                        // Si no tengo precio de ultima factura
                        if ((productoSocio.getPriceInvoiced() == null) || (productoSocio.getPriceInvoiced().compareTo(Env.ZERO) <= 0)){
                            // Si tampoco tengo precio OC
                            if ((productoSocio.getPricePO() == null) || (productoSocio.getPricePO().compareTo(Env.ZERO) <= 0)){
                                productoSocio = MZProductoSocio.getByLastInvoice(getCtx(), product.get_ID(), null);
                            }
                        }
                    }

                    // Si no hay facturas, obtengo socio de ultima gestión de precios de proveedor.
                    if ((productoSocio == null) || (productoSocio.get_ID() <= 0)){
                        productoSocio = MZProductoSocio.getByLastPriceOC(getCtx(), product.get_ID(), null);
                    }

                    if ((productoSocio != null) && (productoSocio.get_ID() > 0)){
                        priceInvoiced = productoSocio.getPriceInvoiced();
                        if ((priceInvoiced == null) || (priceInvoiced.compareTo(Env.ZERO) == 0)){
                            priceInvoiced = productoSocio.getPricePO();
                        }
                        dateInvoiced = productoSocio.getDateInvoiced();
                        cCurrencyID = productoSocio.getC_Currency_ID();
                        if (productoSocio.getC_Invoice_ID() > 0){
                            MInvoice invoiceRef = (MInvoice) productoSocio.getC_Invoice();
                            if (invoiceRef != null){
                                documentNoRef = invoiceRef.getDocumentNo();
                            }
                            else{
                                documentNoRef = "";
                            }
                        }
                    }

                    MInOutLine inOutLine = new MInOutLine(inOut);
                    inOutLine.setM_InOut_ID(inOut.get_ID());
                    inOutLine.set_ValueOfColumn("AD_Client_ID", inOut.getAD_Client_ID());
                    inOutLine.setAD_Org_ID(inOut.getAD_Org_ID());
                    inOutLine.setM_Product_ID(product.get_ID());
                    inOutLine.setC_UOM_ID(product.getC_UOM_ID());

                    String upc = mzmbOrdenDevolLine.getUPC();
                    if ((upc == null) || (upc.trim().equalsIgnoreCase(""))){
                        MZProductoUPC productoUPC = MZProductoUPC.getByProduct(getCtx(), product.get_ID(), null);
                        if ((productoUPC != null) && (productoUPC.get_ID() > 0)){
                            upc = productoUPC.getUPC();
                        }
                    }

                    inOutLine.set_ValueOfColumn("UPC", upc);
                    inOutLine.set_ValueOfColumn("VendorProductNo", vendorProductCode);
                    inOutLine.setQtyEntered(mzmbOrdenDevolLine.getQtyEntered());
                    inOutLine.setMovementQty(mzmbOrdenDevolLine.getQtyEntered());
                    inOutLine.setConfirmedQty(mzmbOrdenDevolLine.getQtyEntered());
                    inOutLine.set_ValueOfColumn("DocumentNoRef", documentNoRef);
                    inOutLine.set_ValueOfColumn("PriceInvoiced", priceInvoiced);
                    inOutLine.set_ValueOfColumn("DateInvoiced", dateInvoiced);
                    inOutLine.set_ValueOfColumn("LineTotalAmt", inOutLine.getMovementQty().multiply(priceInvoiced).setScale(2, RoundingMode.HALF_UP));
                    if (cCurrencyID > 0){
                        inOutLine.set_ValueOfColumn("C_Currency_ID", schema.getC_Currency_ID());
                    }
                    inOutLine.setM_Locator_ID(MLocator.getDefault((MWarehouse) inOut.getM_Warehouse()).get_ID());

                    inOutLine.set_ValueOfColumn("DestinoDevol", "NOTACREDITO");

                    // Si tengo codigo de motivo de devolucion
                    String codMotivoDevol = inOutLine.get_ValueAsString("CodMotivoDevol");
                    if ((codMotivoDevol != null) && (!codMotivoDevol.trim().equalsIgnoreCase(""))){
                        // Si este código lo tengo en la tabla de motivos, guardo el ID en la inoutline.
                        MZMotivoDevol motivoDevol = MZMotivoDevol.getByValue(getCtx(), codMotivoDevol, null);
                        if ((motivoDevol != null) && (motivoDevol.get_ID() > 0)){
                            inOutLine.set_ValueOfColumn("Z_MotivoDevol_ID", motivoDevol.get_ID());
                        }
                    }

                    inOutLine.saveEx();

                    if (cCurrencyID == schema.getC_Currency_ID()){
                        totalAmt = totalAmt.add((BigDecimal)inOutLine.get_Value("LineTotalAmt"));
                    }
                    else{
                        BigDecimal rate = CurrencyUtils.getCurrencyRateToAcctSchemaCurrency(getCtx(), inOut.getAD_Client_ID(),
                                0, cCurrencyID, schema.getC_Currency_ID(), 114, dateInvoiced, null);
                        if (rate == null) rate = Env.ZERO;
                        totalAmt = totalAmt.add(((BigDecimal)inOutLine.get_Value("LineTotalAmt")).multiply(rate).setScale(2, RoundingMode.HALF_UP));
                    }
                }

                // Seteo monto total de la devolución.
                inOut.set_ValueOfColumn("AmtTotal", totalAmt);
                inOut.saveEx();

                // Marco orden móvil como procesada
                mzmbOrdenDevol.setIsExecuted(true);
                mzmbOrdenDevol.setProcessing(false);
                mzmbOrdenDevol.setM_InOut_ID(inOut.get_ID());
                mzmbOrdenDevol.saveEx();
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return "OK";
    }
}
