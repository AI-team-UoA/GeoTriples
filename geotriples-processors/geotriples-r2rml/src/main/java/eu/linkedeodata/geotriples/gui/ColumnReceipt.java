/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.linkedeodata.geotriples.gui;

import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;

/**
 * JavaBean class representing a Stock Quote.
 */
public class ColumnReceipt {
    private String columnName = null;
    private String predicate = null;
    private String dataType = "null";
    private String tableName = "null";
    private TableName d2rqTableName = null;
    private DataType d2rqDataType = null;
    private float highValue = 0;
    private float lowValue = 0;
    private float change = 0;
    private float volume = 0;
    private String transformation="identity";

    public ColumnReceipt(ColumnReceipt columnshp) {
    	this.columnName=columnshp.columnName;
    	this.predicate=columnshp.predicate;
    	this.dataType=columnshp.dataType;
    	this.tableName=columnshp.tableName;
    	this.highValue=columnshp.highValue;
    	this.lowValue=columnshp.lowValue;
    	this.change=columnshp.change;
    	this.volume=columnshp.volume;
	}

	public ColumnReceipt() {
		// do nothing
	}

	public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String symbol) {
        this.columnName = symbol;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String companyName) {
        this.predicate = companyName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String value) {
        this.dataType = value;
    }

    public void setValue(float value) {
        try {
        	setDataType(String.valueOf(value));
        } catch(NumberFormatException exception) {
        	setDataType(String.valueOf(Float.NaN));
        }
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tablename) {
        this.tableName = tablename;
    }

    public float getHighValue() {
        return highValue;
    }

    public void setHighValue(float highValue) {
        this.highValue = highValue;
    }

    public void setHighValue(String highValue) {
        try {
            setHighValue(Float.parseFloat(highValue));
        } catch(NumberFormatException exception) {
            setHighValue(Float.NaN);
        }
    }

    public float getLowValue() {
        return lowValue;
    }

    public void setLowValue(float lowValue) {
        this.lowValue = lowValue;
    }

    public void setLowValue(String lowValue) {
        try {
            setLowValue(Float.parseFloat(lowValue));
        } catch(NumberFormatException exception) {
            setLowValue(Float.NaN);
        }
    }

    public float getChange() {
        return change;
    }

    public void setChange(float change) {
        this.change = change;
    }

    public void setChange(String change) {
        try {
            setChange(Float.parseFloat(change));
        } catch(NumberFormatException exception) {
            setChange(Float.NaN);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setVolume(String volume) {
        try {
            setVolume(Float.parseFloat(volume));
        } catch(NumberFormatException exception) {
            setVolume(Float.NaN);
        }
    }
    @Override
    public String toString() {
    	return columnName;
    }

	public String getTransformation() {
		return transformation;
	}

	public void setTransformation(String transformation) {
		this.transformation = transformation;
	}

	public TableName getD2rqTableName() {
		return d2rqTableName;
	}

	public void setD2rqTableName(TableName d2rqTableName) {
		this.d2rqTableName = d2rqTableName;
	}

	public boolean equals(ColumnReceipt o) {
		return columnName.equalsIgnoreCase(o.getColumnName()) && dataType.equalsIgnoreCase(dataType); 
	}

	public DataType getD2rqDataType() {
		return d2rqDataType;
	}

	public void setD2rqDataType(DataType d2rqDataType) {
		this.d2rqDataType = d2rqDataType;
	}
}
