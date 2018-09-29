package org.xpande.mobile.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.process.DocAction;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.xpande.comercial.model.MZComercialConfig;
import org.xpande.comercial.model.MZOrdenDevolucion;
import org.xpande.comercial.model.MZOrdenDevolucionLin;
import org.xpande.core.model.MZProductoUPC;
import org.xpande.core.utils.CurrencyUtils;
import org.xpande.mobile.model.MZMBInOutFact;
import org.xpande.mobile.model.MZMBInOutLine;
import org.xpande.mobile.model.MZMBOrdenDevol;
import org.xpande.mobile.model.MZMBOrdenDevolLine;
import org.xpande.retail.model.MZProductoSocio;
import org.xpande.retail.model.MZRecepcionProdFact;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

/**
 * Proceso que genera ordenes de devoluciones en Adempiere, a partir de datos generados desde un dispositivo móvil.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 1/3/18.
 */
public class GenerarOrdenDevol extends SvrProcess {

    @Override
    protected void prepare() {

    }

    @Override
    protected String doIt() throws Exception {

        try{

            Timestamp fechaHoy = TimeUtil.trunc(new Timestamp(System.currentTimeMillis()), TimeUtil.TRUNC_DAY);

            // Instancio configurador comercial
            MZComercialConfig comercialConfig = MZComercialConfig.getDefault(getCtx(), get_TrxName());

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

                MDocType[] docTypes = MDocType.getOfDocBaseType(getCtx(), "ODV");
                if (docTypes.length <= 0){
                    throw new AdempiereException("Falta parametrizar documento para Orden de Devolución a Proveedor.");
                }
                MDocType doc = docTypes[0];

                MBPartner partner = new MBPartner(getCtx(), mzmbOrdenDevol.getC_BPartner_ID(), null);

                // Genero cabezal de Orden de Devolución en Adempiere
                MZOrdenDevolucion ordenDevolucion = new MZOrdenDevolucion(getCtx(), 0, get_TrxName());
                ordenDevolucion.set_ValueOfColumn("AD_Client_ID", mzmbOrdenDevol.getAD_Client_ID());
                ordenDevolucion.setAD_Org_ID(mzmbOrdenDevol.getAD_Org_ID());
                ordenDevolucion.setC_BPartner_ID(mzmbOrdenDevol.getC_BPartner_ID());
                ordenDevolucion.setTaxID(partner.getTaxID());
                ordenDevolucion.setC_DocType_ID(doc.get_ID());
                ordenDevolucion.setDateDoc(fechaHoy);
                ordenDevolucion.setDescription(mzmbOrdenDevol.getDescription());
                ordenDevolucion.setM_Warehouse_ID(warehouse.get_ID());
                ordenDevolucion.setMovementDate(mzmbOrdenDevol.getMovementDate());
                ordenDevolucion.setMovementType(mzmbOrdenDevol.getMovementType());
                ordenDevolucion.setC_Currency_ID(schema.getC_Currency_ID());
                ordenDevolucion.saveEx();

                // Genero lineas para esta orden de devolución en Adempiere
                BigDecimal totalAmt = Env.ZERO;


                List<MZMBOrdenDevolLine> ordenDevolLineList = mzmbOrdenDevol.getLines();
                for (MZMBOrdenDevolLine mzmbOrdenDevolLine: ordenDevolLineList){

                    MProduct product = new MProduct(getCtx(), mzmbOrdenDevolLine.getM_Product_ID(), null);

                    BigDecimal priceInvoiced = Env.ZERO;
                    Timestamp dateInvoiced = null;
                    int cCurrencyID = 0;
                    // Instancio modelo de producto-socio para obtener datos de ultima factura
                    MZProductoSocio productoSocio = MZProductoSocio.getByBPartnerProduct(getCtx(), ordenDevolucion.getC_BPartner_ID(), product.get_ID(), null);
                    // Si no tengo modelo para este socio de negocio de la ultima factura
                    if ((productoSocio == null) || (productoSocio.get_ID() <= 0)){
                        productoSocio = MZProductoSocio.getByLastInvoice(getCtx(), product.get_ID(), null);
                    }
                    else{
                        // Si no tengo precio de ultima factura
                        if ((productoSocio.getPriceInvoiced() != null) && (productoSocio.getPriceInvoiced().compareTo(Env.ZERO) > 0)){
                            productoSocio = MZProductoSocio.getByLastInvoice(getCtx(), product.get_ID(), null);
                        }
                    }
                    // Si no hay facturas, obtengo socio de ultima gestión de precios de proveedor.
                    if ((productoSocio == null) || (productoSocio.get_ID() <= 0)){
                        productoSocio = MZProductoSocio.getByLastPriceOC(getCtx(), product.get_ID(), null);
                    }

                    if ((productoSocio != null) && (productoSocio.get_ID() > 0)){
                        priceInvoiced = productoSocio.getPriceInvoiced();
                        dateInvoiced = productoSocio.getDateInvoiced();
                        cCurrencyID = productoSocio.getC_Currency_ID();
                    }

                    MZOrdenDevolucionLin devolucionLin = new MZOrdenDevolucionLin(getCtx(), 0, get_TrxName());
                    devolucionLin.set_ValueOfColumn("AD_Client_ID", ordenDevolucion.getAD_Client_ID());
                    devolucionLin.setAD_Org_ID(ordenDevolucion.getAD_Org_ID());
                    devolucionLin.setM_Product_ID(product.get_ID());
                    devolucionLin.setC_UOM_ID(product.getC_UOM_ID());
                    devolucionLin.setUPC(mzmbOrdenDevolLine.getUPC());
                    devolucionLin.setMovementQty(mzmbOrdenDevolLine.getQtyEntered());
                    devolucionLin.setZ_OrdenDevolucion_ID(ordenDevolucion.get_ID());
                    devolucionLin.setPriceInvoiced(priceInvoiced);
                    devolucionLin.setDateInvoiced(dateInvoiced);
                    devolucionLin.setLineTotalAmt(devolucionLin.getMovementQty().multiply(devolucionLin.getPriceInvoiced()).setScale(2, RoundingMode.HALF_UP));
                    if (cCurrencyID > 0) devolucionLin.setC_Currency_ID(cCurrencyID);
                    devolucionLin.saveEx();

                    if (cCurrencyID == ordenDevolucion.getC_Currency_ID()){
                        totalAmt = totalAmt.add(devolucionLin.getLineTotalAmt());
                    }
                    else{
                        BigDecimal rate = CurrencyUtils.getCurrencyRateToAcctSchemaCurrency(getCtx(), ordenDevolucion.getAD_Client_ID(),
                                0, cCurrencyID, schema.getC_Currency_ID(), 114, devolucionLin.getDateInvoiced(), null);
                        if (rate == null) rate = Env.ZERO;
                        totalAmt = totalAmt.add(devolucionLin.getLineTotalAmt().multiply(rate).setScale(2, RoundingMode.HALF_UP));
                    }
                }

                // Seteo total de la orden de devolución.
                ordenDevolucion.setAmtTotal(totalAmt);

                // Completo orden de devolución en Adempiere
                if (!ordenDevolucion.processIt(DocAction.ACTION_Complete)){
                    mzmbOrdenDevol.setErrorMsg("No se pudo Completar Orden de Devolución " + ordenDevolucion.getDocumentNo() + " : " + ordenDevolucion.getProcessMsg());
                    mzmbOrdenDevol.setProcessing(false);
                    mzmbOrdenDevol.saveEx();

                    ordenDevolucion.deleteEx(true);

                    continue;
                }
                ordenDevolucion.saveEx();

                // Marco orden móvil como procesada
                mzmbOrdenDevol.setIsExecuted(true);
                mzmbOrdenDevol.setProcessing(false);
                mzmbOrdenDevol.setZ_OrdenDevolucion_ID(ordenDevolucion.get_ID());
                mzmbOrdenDevol.saveEx();

            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return "OK";
    }
}
