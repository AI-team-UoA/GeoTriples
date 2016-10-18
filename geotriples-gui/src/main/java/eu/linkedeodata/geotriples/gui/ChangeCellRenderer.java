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

import java.awt.Color;

import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.content.TableViewNumberCellRenderer;

/**
 * Table view cell renderer that presents a formatted change value.
 */
public class ChangeCellRenderer extends TableViewNumberCellRenderer {
    public static final Color UP_COLOR = new Color(0x00, 0x80, 0x00);
    public static final Color DOWN_COLOR = new Color(0xff, 0x00, 0x00);

    @Override
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        super.render(row, rowIndex, columnIndex, tableView, columnName,
            selected, highlighted, disabled);

        if (row != null
            && !selected) {
            ColumnReceipt stockQuote = (ColumnReceipt)row;
            float change = stockQuote.getChange();
            getStyles().put("color", change < 0 ? DOWN_COLOR : UP_COLOR);
        }
    }
}
