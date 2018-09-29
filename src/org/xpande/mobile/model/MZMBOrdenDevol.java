package org.xpande.mobile.model;

import org.compiere.model.Query;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 * Clase para cabezales mobile de ordenes de devolución a proveedores.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 1/3/18.
 */
public class MZMBOrdenDevol extends X_Z_MB_OrdenDevol {

    public MZMBOrdenDevol(Properties ctx, int Z_MB_OrdenDevol_ID, String trxName) {
        super(ctx, Z_MB_OrdenDevol_ID, trxName);
    }

    public MZMBOrdenDevol(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }


    /***
     * Obtiene y retorna cabezales moviles de ordenes de devolución no procesadas aún.
     * Xpande. Created by Gabriel Vila on 1/3/18.
     * @param ctx
     * @param trxName
     * @return
     */
    public static List<MZMBOrdenDevol> getNotExecuted(Properties ctx, String trxName){

        String whereClause = X_Z_MB_OrdenDevol.COLUMNNAME_IsExecuted + " ='N' ";
        //" AND " + X_Z_MB_InOut.COLUMNNAME_Processing + " ='N' ";

        List<MZMBOrdenDevol> lines = new Query(ctx, I_Z_MB_OrdenDevol.Table_Name, whereClause, trxName).list();

        return lines;
    }


    /***
     * Obtiene y retorna lineas asociadas a esta orden de devolución móvil.
     * Xpande. Created by Gabriel Vila on 1/3/18.
     * @return
     */
    public List<MZMBOrdenDevolLine> getLines(){

        String whereClause = X_Z_MB_OrdenDevolLine.COLUMNNAME_Z_MB_OrdenDevol_ID + " =" + this.get_ID();

        List<MZMBOrdenDevolLine> lines = new Query(getCtx(), I_Z_MB_OrdenDevolLine.Table_Name, whereClause, get_TrxName()).list();

        return lines;
    }

}
