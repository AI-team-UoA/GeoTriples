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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import org.apache.pivot.wtk.validation.Validator;

/**
 * Validates that text represents a valid dollar value.
 */
public class CurrencyValidator implements Validator {
    protected static final DecimalFormat FORMAT = new DecimalFormat("0.00");
    static {
        FORMAT.setParseBigDecimal(true);
    }

    @Override
    public boolean isValid(String text) {
        boolean valid = true;

        if (text.length() > 0) {
            ParsePosition parsePosition = new ParsePosition(0);
            BigDecimal numericAmount = (BigDecimal) FORMAT.parse(text, parsePosition);
            valid = (numericAmount != null &&
                numericAmount.scale() <= 2 &&
                numericAmount.signum() >= 0 &&
                parsePosition.getErrorIndex() == -1 &&
                parsePosition.getIndex() == text.length());
        }

        return valid;
    }
}
