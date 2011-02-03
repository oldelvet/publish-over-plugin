/*
 * The MIT License
 *
 * Copyright (C) 2010-2011 by Anthony Robinson
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

package jenkins.plugins.publish_over;

import hudson.Util;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.IOException;
import java.io.Serializable;

public abstract class BPHostConfiguration<CLIENT extends BPClient, COMMON_CONFIG> implements Serializable {

    private String name;
	private String hostname;
    private String username;
    private String password;
    private String remoteRootDir;
    private int port;
    private COMMON_CONFIG commonConfig;

	public BPHostConfiguration() {}

	public BPHostConfiguration(String name, String hostname, String username, String password, String remoteRootDir, int port) {
		this.name = name;
		this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.remoteRootDir = remoteRootDir;
        this.port = port;
	}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRemoteRootDir() { return remoteRootDir; }
    public void setRemoteRootDir(String remoteRootDir) { this.remoteRootDir = remoteRootDir; }

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
    
    public COMMON_CONFIG getCommonConfig() { return commonConfig; }
    public void setCommonConfig(COMMON_CONFIG commonConfig) { this.commonConfig = commonConfig; }

    public abstract CLIENT createClient(BPBuildInfo buildInfo) throws BapPublisherException;

    protected boolean isDirectoryAbsolute(String directory) {
        if (directory == null)
            return false;
        return directory.startsWith("/") || directory.startsWith("\\");
    }
    
    protected void changeToRootDirectory(BPClient client) throws IOException {
        String remoteRootDir = getRemoteRootDir();
        if (Util.fixEmptyAndTrim(remoteRootDir) != null) {
            if (!client.changeDirectory(remoteRootDir))
                exception(client, Messages.exception_cwdRemoteRoot(remoteRootDir));
        }
    }
    
    protected void exception(BPClient client, String message) {
        BapPublisherException.exception(client, message);
    }
    
    protected HashCodeBuilder createHashCodeBuilder() {
        return addToHashCode(new HashCodeBuilder());
    }

    protected HashCodeBuilder addToHashCode(HashCodeBuilder builder) {
        return builder.append(name)
            .append(hostname)
            .append(username)
            .append(password)
            .append(remoteRootDir)
            .append(commonConfig)
            .append(port);
    }
    
    protected EqualsBuilder createEqualsBuilder(BPHostConfiguration that) {
        return addToEquals(new EqualsBuilder(), that);
    }
    
    protected EqualsBuilder addToEquals(EqualsBuilder builder, BPHostConfiguration that) {
        return builder.append(name, that.name)
            .append(hostname, that.hostname)
            .append(username, that.username)
            .append(password, that.password)
            .append(remoteRootDir, that.remoteRootDir)
            .append(commonConfig, that.commonConfig)
            .append(port, that.port);
    }
    
    protected ToStringBuilder addToToString(ToStringBuilder builder) {
        return builder.append("name", name)
            .append("hostname", hostname)
            .append("username", username)
            .append("password", "***")
            .append("remoteRootDir", remoteRootDir)
            .append("commonConfig", commonConfig)
            .append("port", port);
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        return createEqualsBuilder((BPHostConfiguration) o).isEquals();
    }

    public int hashCode() {
        return createHashCodeBuilder().toHashCode();
    }
    
    public String toString() {
        return addToToString(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

}
