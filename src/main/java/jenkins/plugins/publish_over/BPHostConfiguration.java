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
import hudson.util.Secret;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.IOException;
import java.io.Serializable;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class BPHostConfiguration<CLIENT extends BPClient, COMMON_CONFIG> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String hostname;
    private String username;
    private String password;
    private Secret secretPassword;
    private String remoteRootDir;
    private int port;
    private COMMON_CONFIG commonConfig;

    public BPHostConfiguration() { }

    public BPHostConfiguration(final String name, final String hostname, final String username, final String password,
                               final String remoteRootDir, final int port) {
        this.name = name;
        this.hostname = hostname;
        this.username = username;
        secretPassword = Secret.fromString(password);
        this.remoteRootDir = remoteRootDir;
        this.port = port;
    }

    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }

    public String getHostname() { return hostname; }
    public void setHostname(final String hostname) { this.hostname = hostname; }
    // current bug in Jenkins (prototype/json-lib/stapler) will leave quotes around strings that start and end with brackets or braces
    // this method will allow a "hack" to add a space at the end of a String, which will be stripped before use - the std. accessor
    // is kept to ensure that the original String can persist through re-configurations.
    // this is an awful hack to enable an IPv6 address to be used as a hostname
    public String getHostnameTrimmed() {
        return Util.fixEmptyAndTrim(hostname);
    }

    public String getUsername() { return username; }
    public void setUsername(final String username) { this.username = username; }

    protected String getPassword() { return Secret.toString(secretPassword); }
    public void setPassword(final String password) { secretPassword = Secret.fromString(password); }

    public String getEncryptedPassword() {
        return (secretPassword == null) ? null : secretPassword.getEncryptedValue();
    }

    public String getRemoteRootDir() { return remoteRootDir; }
    public void setRemoteRootDir(final String remoteRootDir) { this.remoteRootDir = remoteRootDir; }

    public int getPort() { return port; }
    public void setPort(final int port) { this.port = port; }

    public COMMON_CONFIG getCommonConfig() { return commonConfig; }
    public void setCommonConfig(final COMMON_CONFIG commonConfig) { this.commonConfig = commonConfig; }

    public CLIENT createClient(final BPBuildInfo buildInfo, final BapPublisher publisher) {
        return createClient(buildInfo);
    }

    public abstract CLIENT createClient(BPBuildInfo buildInfo);

    protected boolean isDirectoryAbsolute(final String directory) {
        if ((directory == null) || (directory.length() < 1))
            return false;
        final char first = directory.charAt(0);
        return (first == '/') || (first == '\\');
    }

    protected void changeToRootDirectory(final BPClient client) throws IOException {
        final String remoteRootDir = getRemoteRootDir();
        if ((Util.fixEmptyAndTrim(remoteRootDir) != null) && (!client.changeDirectory(remoteRootDir))) {
                exception(client, Messages.exception_cwdRemoteRoot(remoteRootDir));
        }
    }

    protected void exception(final BPClient client, final String message) {
        BapPublisherException.exception(client, message);
    }

    protected HashCodeBuilder addToHashCode(final HashCodeBuilder builder) {
        return builder.append(name)
            .append(hostname)
            .append(username)
            .append(secretPassword)
            .append(remoteRootDir)
            .append(commonConfig)
            .append(port);
    }

    protected EqualsBuilder addToEquals(final EqualsBuilder builder, final BPHostConfiguration that) {
        return builder.append(name, that.name)
            .append(hostname, that.hostname)
            .append(username, that.username)
            .append(secretPassword, that.secretPassword)
            .append(remoteRootDir, that.remoteRootDir)
            .append(commonConfig, that.commonConfig)
            .append(port, that.port);
    }

    protected ToStringBuilder addToToString(final ToStringBuilder builder) {
        return builder.append("name", name)
            .append("hostname", hostname)
            .append("username", username)
            .append("remoteRootDir", remoteRootDir)
            .append("commonConfig", commonConfig)
            .append("port", port);
    }

    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;

        return addToEquals(new EqualsBuilder(), (BPHostConfiguration) that).isEquals();
    }

    public int hashCode() {
        return addToHashCode(new HashCodeBuilder()).toHashCode();
    }

    public String toString() {
        return addToToString(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

    public Object readResolve() {
        if (secretPassword == null)
            secretPassword = Secret.fromString(password);
        password = null;
        return this;
    }

}
