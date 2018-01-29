package org.xpande.mobile.model;

import org.compiere.model.Query;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 * Modelo para cabezales InOut de datos móbiles.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 12/15/17.
 */
public class MZMBInOut extends X_Z_MB_InOut {

    public MZMBInOut(Properties ctx, int Z_MB_InOut_ID, String trxName) {
        super(ctx, Z_MB_InOut_ID, trxName);
    }

    public MZMBInOut(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    /***
     * Obtengo y retorno lista de recepciones moviles no procesadas.
     * Xpande. Created by Gabriel Vila on 12/15/17.
     * @param ctx
     * @param trxName
     * @return
     */
    public static List<MZMBInOut> getNotExecuted(Properties ctx, String trxName){

        String whereClause = X_Z_MB_InOut.COLUMNNAME_IsExecuted + " ='N' ";
                //" AND " + X_Z_MB_InOut.COLUMNNAME_Processing + " ='N' ";

        List<MZMBInOut> lines = new Query(ctx, I_Z_MB_InOut.Table_Name, whereClause, trxName).setOrderBy(" Z_MB_InOut_ID ").list();

        return lines;
    }


    /***
     * Obtiene y retorna lista de facturas asociadas a esta recepcion movil.
     * Xpande. Created by Gabriel Vila on 12/15/17.
     * @return
     */
    public List<MZMBInOutFact> getFacturasAsociadas(){

        String whereClause = X_Z_MB_InOutFact.COLUMNNAME_Z_MB_InOut_ID + " =" + this.get_ID();

        List<MZMBInOutFact> lines = new Query(getCtx(), I_Z_MB_InOutFact.Table_Name, whereClause, get_TrxName()).setOrderBy(" Z_MB_InOutFact_ID ").list();

        return lines;
    }


    /***
     * Obtiene y retorna lineas asociadas a esta recepcion movil.
     * Xpande. Created by Gabriel Vila on 12/15/17.
     * @return
     */
    public List<MZMBInOutLine> getLines(){

        String whereClause = X_Z_MB_InOutLine.COLUMNNAME_Z_MB_InOut_ID + " =" + this.get_ID();

        List<MZMBInOutLine> lines = new Query(getCtx(), I_Z_MB_InOutLine.Table_Name, whereClause, get_TrxName()).list();

        return lines;
    }


}
