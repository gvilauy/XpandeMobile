/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.xpande.mobile.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for Z_MB_InOutFact
 *  @author Adempiere (generated) 
 *  @version Release 3.9.0 - $Id$ */
public class X_Z_MB_InOutFact extends PO implements I_Z_MB_InOutFact, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20171215L;

    /** Standard Constructor */
    public X_Z_MB_InOutFact (Properties ctx, int Z_MB_InOutFact_ID, String trxName)
    {
      super (ctx, Z_MB_InOutFact_ID, trxName);
      /** if (Z_MB_InOutFact_ID == 0)
        {
			setC_Currency_ID (0);
			setDateInvoiced (new Timestamp( System.currentTimeMillis() ));
			setDocumentNoRef (null);
			setDocumentSerie (null);
			setZ_MB_InOutFact_ID (0);
			setZ_MB_InOut_ID (0);
        } */
    }

    /** Load Constructor */
    public X_Z_MB_InOutFact (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_Z_MB_InOutFact[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_C_Currency getC_Currency() throws RuntimeException
    {
		return (I_C_Currency)MTable.get(getCtx(), I_C_Currency.Table_Name)
			.getPO(getC_Currency_ID(), get_TrxName());	}

	/** Set Currency.
		@param C_Currency_ID 
		The Currency for this record
	  */
	public void setC_Currency_ID (int C_Currency_ID)
	{
		if (C_Currency_ID < 1) 
			set_Value (COLUMNNAME_C_Currency_ID, null);
		else 
			set_Value (COLUMNNAME_C_Currency_ID, Integer.valueOf(C_Currency_ID));
	}

	/** Get Currency.
		@return The Currency for this record
	  */
	public int getC_Currency_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Currency_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date Invoiced.
		@param DateInvoiced 
		Date printed on Invoice
	  */
	public void setDateInvoiced (Timestamp DateInvoiced)
	{
		set_Value (COLUMNNAME_DateInvoiced, DateInvoiced);
	}

	/** Get Date Invoiced.
		@return Date printed on Invoice
	  */
	public Timestamp getDateInvoiced () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateInvoiced);
	}

	/** Set DocumentNoRef.
		@param DocumentNoRef 
		Numero de documento referenciado
	  */
	public void setDocumentNoRef (String DocumentNoRef)
	{
		set_Value (COLUMNNAME_DocumentNoRef, DocumentNoRef);
	}

	/** Get DocumentNoRef.
		@return Numero de documento referenciado
	  */
	public String getDocumentNoRef () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNoRef);
	}

	/** Set DocumentSerie.
		@param DocumentSerie 
		Serie de un Documento
	  */
	public void setDocumentSerie (String DocumentSerie)
	{
		set_Value (COLUMNNAME_DocumentSerie, DocumentSerie);
	}

	/** Get DocumentSerie.
		@return Serie de un Documento
	  */
	public String getDocumentSerie () 
	{
		return (String)get_Value(COLUMNNAME_DocumentSerie);
	}

	/** Set Z_MB_InOutFact ID.
		@param Z_MB_InOutFact_ID Z_MB_InOutFact ID	  */
	public void setZ_MB_InOutFact_ID (int Z_MB_InOutFact_ID)
	{
		if (Z_MB_InOutFact_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_Z_MB_InOutFact_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Z_MB_InOutFact_ID, Integer.valueOf(Z_MB_InOutFact_ID));
	}

	/** Get Z_MB_InOutFact ID.
		@return Z_MB_InOutFact ID	  */
	public int getZ_MB_InOutFact_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_MB_InOutFact_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_Z_MB_InOut getZ_MB_InOut() throws RuntimeException
    {
		return (I_Z_MB_InOut)MTable.get(getCtx(), I_Z_MB_InOut.Table_Name)
			.getPO(getZ_MB_InOut_ID(), get_TrxName());	}

	/** Set Z_MB_InOut ID.
		@param Z_MB_InOut_ID Z_MB_InOut ID	  */
	public void setZ_MB_InOut_ID (int Z_MB_InOut_ID)
	{
		if (Z_MB_InOut_ID < 1) 
			set_Value (COLUMNNAME_Z_MB_InOut_ID, null);
		else 
			set_Value (COLUMNNAME_Z_MB_InOut_ID, Integer.valueOf(Z_MB_InOut_ID));
	}

	/** Get Z_MB_InOut ID.
		@return Z_MB_InOut ID	  */
	public int getZ_MB_InOut_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_MB_InOut_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}