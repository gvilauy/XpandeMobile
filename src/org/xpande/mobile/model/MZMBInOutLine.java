package org.xpande.mobile.model;

import java.sql.ResultSet;
import java.util.Properties;

/**
 * Modelo de lineas inOutLine para datos móbiles.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 12/15/17.
 */
public class MZMBInOutLine extends X_Z_MB_InOutLine {

    public MZMBInOutLine(Properties ctx, int Z_MB_InOutLine_ID, String trxName) {
        super(ctx, Z_MB_InOutLine_ID, trxName);
    }

    public MZMBInOutLine(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }
}
