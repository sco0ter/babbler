/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.sample;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @author Christian Schudt
 */
public class LogFormatter extends SimpleFormatter {

    @Override
    public synchronized String format(LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dateFormat = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime resultDate =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(logRecord.getMillis()), ZoneId.systemDefault());
        sb.append(dateFormat.format(resultDate))
                .append(' ')
                .append(logRecord.getLevel())
                .append("  ")
                .append(formatMessage(logRecord));
        if (logRecord.getThrown() != null) {
            logRecord.getThrown().printStackTrace();
        }
        sb.append('\n');
        return sb.toString();
    }
}
