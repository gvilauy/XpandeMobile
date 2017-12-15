package org.xpande.mobile.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.process.DocAction;
import org.compiere.process.SvrProcess;
import org.xpande.comercial.model.MZComercialConfig;
import org.xpande.mobile.model.MZMBInOut;
import org.xpande.mobile.model.MZMBInOutFact;
import org.xpande.mobile.model.MZMBInOutLine;
import org.xpande.retail.model.MZRecepcionProdFact;

import java.util.HashMap;
import java.util.List;

/**
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
                    recepcionProdFact.setDocumentSerie(mzmbInOutFact.getDocumentSerie());
                    recepcionProdFact.setManualDocumentNo(mzmbInOutFact.getDocumentNoRef());
                    recepcionProdFact.saveEx();

                    hashFacturasAsociadas.put(recepcionProdFact.getManualDocumentNo(), recepcionProdFact.get_ID());

                }

                int lineNo = 0;

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
                    inOutLine.set_ValueOfColumn("Z_RecepcionProdFact_ID", hashFacturasAsociadas.get(mzmbInOutLine.getDocumentNoRef()).intValue());
                    inOutLine.set_ValueOfColumn("QtyEnteredInvoice", mzmbInOutLine.getQtyInvoiced());
                    inOutLine.saveEx();

                }

                // Completo recepcion en Adempiere
                if (!inOut.processIt(DocAction.ACTION_Complete)){
                    mzmbInOut.setErrorMsg("No se pudo Completar Recepcion " + inOut.getDocumentNo() + " : " + inOut.getProcessMsg());
                    mzmbInOut.setProcessing(false);
                    mzmbInOut.saveEx();
                    continue;
                }
                inOut.saveEx();

                // Genero
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return "OK";
    }
}
