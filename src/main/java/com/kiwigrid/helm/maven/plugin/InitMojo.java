package com.kiwigrid.helm.maven.plugin;

import java.io.File;
import java.net.PasswordAuthentication;

import com.kiwigrid.helm.maven.plugin.pojo.HelmRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

/**
 * Mojo for initializing helm
 *
 * @author Fabian Schlegel
 * @since 06.11.17
 */
@Mojo(name = "init", defaultPhase = LifecyclePhase.INITIALIZE)
public class InitMojo extends AbstractHelmMojo {

	@Parameter(property = "helm.init.skipRefresh")
	private boolean skipRefresh;

	public void execute()
			throws MojoExecutionException
	{
		getLog().info("Initializing Helm...");
		getLog().info("Creating output directory...");
		callCli("mkdir -p " + getOutputDirectory(), "Unable to create output directory at " + getOutputDirectory(),
				false);
		getLog().info("Downloading Helm...");
		callCli("wget -qO "
						+ getHelmExecuteableDirectory()
						+ File.separator
						+ "helm.tar.gz "
						+ getHelmDownloadUrl(),
				"Unable to download helm", false);
		getLog().info("Unpacking Helm...");
		callCli("tar -xf "
				+ getHelmExecuteableDirectory()
				+ File.separator
				// flatten directory structure using --strip to get helm executeable on basedir, see https://www.systutorials.com/docs/linux/man/1-tar/#lbAS
				+ "helm.tar.gz --strip=1 --directory="
				+ getHelmExecuteableDirectory(), "Unable to unpack helm to " + getHelmExecuteableDirectory(), false);
		getLog().info("Run helm init...");
		callCli(getHelmExecuteableDirectory()
						+ File.separator
						+ "helm init --client-only" + (skipRefresh ? " --skip-refresh" : "")
						+ (StringUtils.isNotEmpty(getHelmHomeDirectory()) ? " --home=" + getHelmHomeDirectory() : ""),
				"Unable to call helm init",
				false);

		if (getHelmExtraRepos() != null) {
			for (HelmRepository repository : getHelmExtraRepos()) {
				getLog().info("Adding repo " + repository);
				PasswordAuthentication auth = getAuthentication(repository);
				callCli(getHelmExecuteableDirectory()
								+ File.separator
								+ "helm repo add "
								+ repository.getName()
								+ " "
								+ repository.getUrl()
								+ (StringUtils.isNotEmpty(getHelmHomeDirectory()) ? " --home=" + getHelmHomeDirectory() : "")
								+ (auth != null ? " --username=" + auth.getUserName() + " --password=" + String.valueOf(auth.getPassword()) : ""),
						"Unable add repo",
						false);
			}
		}
	}

	public boolean isSkipRefresh() {
		return skipRefresh;
	}

	public void setSkipRefresh(boolean skipRefresh) {
		this.skipRefresh = skipRefresh;
	}
}
