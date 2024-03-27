/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * StaticServer.java is part of Cool Request
 *
 * License: GPL-3.0+
 *
 * Cool Request is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Request is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Request.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cool.request.components.staticServer;

public class StaticServer {
    private String id; //uuid
    private int port;
    private String root;
    private boolean listDir;
    public StaticServer(String id, int port, String root) {
        this.id = id;
        this.port = port;
        this.root = root;
    }

    public StaticServer() {
    }

    public boolean isListDir() {
        return listDir;
    }

    public void setListDir(boolean listDir) {
        this.listDir = listDir;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
