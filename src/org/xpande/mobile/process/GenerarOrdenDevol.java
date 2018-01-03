package org.xpande.mobile.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.process.DocAction;
import org.compiere.process.SvrProcess;
import org.compiere.util.TimeUtil;
import org.xpande.comercial.model.MZComercialConfig;
import org.xpande.comercial.model.MZOrdenDevolucion;
import org.xpande.comercial.model.MZOrdenDevolucionLin;
import org.xpande.core.model.MZProductoUPC;
import org.xpande.mobile.model.MZMBInOutFact;
import org.xpande.mobile.model.MZMBInOutLine;
import org.xpande.mobile.model.MZMBOrdenDevol;
import org.xpande.mobile.model.MZMBOrdenDevolLine;
import org.xpande.retail.model.MZProductoSocio;
import org.xpande.retail.model.MZRecepcionProdFact;

import java.sql.Timestamp;
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
                ordenDevolucion.setC_DocType_ID(doc.get_ID());
                ordenDevolucion.setDateDoc(fechaHoy);
                ordenDevolucion.setDescription(mzmbOrdenDevol.getDescription());
                ordenDevolucion.setM_Warehouse_ID(warehouse.get_ID());
                ordenDevolucion.setMovementDate(mzmbOrdenDevol.getMovementDate());
                ordenDevolucion.setMovementType(mzmbOrdenDevol.getMovementType());
                ordenDevolucion.saveEx();

                // Genero lineas para esta orden de devolución en Adempiere
                List<MZMBOrdenDevolLine> ordenDevolLineList = mzmbOrdenDevol.getLines();
                for (MZMBOrdenDevolLine mzmbOrdenDevolLine: ordenDevolLineList){

                    MProduct product = new MProduct(getCtx(), mzmbOrdenDevolLine.getM_Product_ID(), null);

                    MZOrdenDevolucionLin devolucionLin = new MZOrdenDevolucionLin(getCtx(), 0, get_TrxName());
                    devolucionLin.set_ValueOfColumn("AD_Client_ID", ordenDevolucion.getAD_Client_ID());
                    devolucionLin.setAD_Org_ID(ordenDevolucion.getAD_Org_ID());
                    devolucionLin.setM_Product_ID(product.get_ID());
                    devolucionLin.setC_UOM_ID(product.getC_UOM_ID());
                    devolucionLin.setUPC(mzmbOrdenDevolLine.getUPC());
                    devolucionLin.setMovementQty(mzmbOrdenDevolLine.getQtyEntered());
                    devolucionLin.setZ_OrdenDevolucion_ID(ordenDevolucion.get_ID());
                    devolucionLin.saveEx();
                }

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
