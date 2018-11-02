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
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for Z_MB_InOutUPC
 *  @author Adempiere (generated) 
 *  @version Release 3.9.0 - $Id$ */
public class X_Z_MB_InOutUPC extends PO implements I_Z_MB_InOutUPC, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20181102L;

    /** Standard Constructor */
    public X_Z_MB_InOutUPC (Properties ctx, int Z_MB_InOutUPC_ID, String trxName)
    {
      super (ctx, Z_MB_InOutUPC_ID, trxName);
      /** if (Z_MB_InOutUPC_ID == 0)
        {
			setM_Product_ID (0);
			setUPC (null);
			setZ_MB_InOut_ID (0);
			setZ_MB_InOutLine_ID (0);
			setZ_MB_InOutUPC_ID (0);
        } */
    }

    /** Load Constructor */
    public X_Z_MB_InOutUPC (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_Z_MB_InOutUPC[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_M_Product getM_Product() throws RuntimeException
    {
		return (I_M_Product)MTable.get(getCtx(), I_M_Product.Table_Name)
			.getPO(getM_Product_ID(), get_TrxName());	}

	/** Set Product.
		@param M_Product_ID 
		Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_Value (COLUMNNAME_M_Product_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set UPC/EAN.
		@param UPC 
		Bar Code (Universal Product Code or its superset European Article Number)
	  */
	public void setUPC (String UPC)
	{
		set_Value (COLUMNNAME_UPC, UPC);
	}

	/** Get UPC/EAN.
		@return Bar Code (Universal Product Code or its superset European Article Number)
	  */
	public String getUPC () 
	{
		return (String)get_Value(COLUMNNAME_UPC);
	}

	/** Set Immutable Universally Unique Identifier.
		@param UUID 
		Immutable Universally Unique Identifier
	  */
	public void setUUID (String UUID)
	{
		set_Value (COLUMNNAME_UUID, UUID);
	}

	/** Get Immutable Universally Unique Identifier.
		@return Immutable Universally Unique Identifier
	  */
	public String getUUID () 
	{
		return (String)get_Value(COLUMNNAME_UUID);
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

	public I_Z_MB_InOutLine getZ_MB_InOutLine() throws RuntimeException
    {
		return (I_Z_MB_InOutLine)MTable.get(getCtx(), I_Z_MB_InOutLine.Table_Name)
			.getPO(getZ_MB_InOutLine_ID(), get_TrxName());	}

	/** Set Z_MB_InOutLine ID.
		@param Z_MB_InOutLine_ID Z_MB_InOutLine ID	  */
	public void setZ_MB_InOutLine_ID (int Z_MB_InOutLine_ID)
	{
		if (Z_MB_InOutLine_ID < 1) 
			set_Value (COLUMNNAME_Z_MB_InOutLine_ID, null);
		else 
			set_Value (COLUMNNAME_Z_MB_InOutLine_ID, Integer.valueOf(Z_MB_InOutLine_ID));
	}

	/** Get Z_MB_InOutLine ID.
		@return Z_MB_InOutLine ID	  */
	public int getZ_MB_InOutLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_MB_InOutLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Z_MB_InOutUPC ID.
		@param Z_MB_InOutUPC_ID Z_MB_InOutUPC ID	  */
	public void setZ_MB_InOutUPC_ID (int Z_MB_InOutUPC_ID)
	{
		if (Z_MB_InOutUPC_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_Z_MB_InOutUPC_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Z_MB_InOutUPC_ID, Integer.valueOf(Z_MB_InOutUPC_ID));
	}

	/** Get Z_MB_InOutUPC ID.
		@return Z_MB_InOutUPC ID	  */
	public int getZ_MB_InOutUPC_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_MB_InOutUPC_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}