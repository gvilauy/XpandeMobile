package org.xpande.mobile.model;

import java.sql.ResultSet;
import java.util.Properties;

/**
 * Clase para lineas mobile de ordenes de devolución a proveedores.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 1/3/18.
 */
public class MZMBOrdenDevolLine extends X_Z_MB_OrdenDevolLine {

    public MZMBOrdenDevolLine(Properties ctx, int Z_MB_OrdenDevolLine_ID, String trxName) {
        super(ctx, Z_MB_OrdenDevolLine_ID, trxName);
    }

    public MZMBOrdenDevolLine(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }
}
