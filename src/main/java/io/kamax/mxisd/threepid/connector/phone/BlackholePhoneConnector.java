/*
 * mxisd - Matrix Identity Server Daemon
 * Copyright (C) 2018 Kamax Sàrl
 *
 * https://www.kamax.io/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.mxisd.threepid.connector.phone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackholePhoneConnector implements PhoneConnector {

    public static final String ID = "none";
    private static final Logger log = LoggerFactory.getLogger("App");

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void send(String recipient, String content) {
        //dev/null
        log.info("verification token is  {}", content);
    }

}
