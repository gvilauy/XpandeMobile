package org.xpande.mobile.model;

import java.sql.ResultSet;
import java.util.Properties;

/**
 * Modelo para factura asociada a inOut en datos móbiles.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 12/15/17.
 */
public class MZMBInOutFact extends X_Z_MB_InOutFact {

    public MZMBInOutFact(Properties ctx, int Z_MB_InOutFact_ID, String trxName) {
        super(ctx, Z_MB_InOutFact_ID, trxName);
    }

    public MZMBInOutFact(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }
}
