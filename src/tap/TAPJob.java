package tap;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012,2014 - UDS/Centre de Données astronomiques de Strasbourg (CDS),
 *                       Astronomisches Rechen Institut (ARI)
 */

import java.util.List;

import tap.parameters.DALIUpload;
import tap.parameters.TAPParameters;
import uws.UWSException;
import uws.job.ErrorSummary;
import uws.job.Result;
import uws.job.UWSJob;
import uws.job.parameters.UWSParameters;
import uws.job.user.JobOwner;

/**
 * <p>Description of a TAP job. This class is used for asynchronous but also synchronous queries.</p>
 * 
 * <p>
 * 	On the contrary to {@link UWSJob}, it is loading parameters from {@link TAPParameters} instances rather than {@link UWSParameters}.
 * 	However, {@link TAPParameters} is an extension of {@link UWSParameters}. That's what allow the UWS library to use both {@link TAPJob} and {@link TAPParameters}.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS;ARI)
 * @version 2.0 (12/2014)
 */
public class TAPJob extends UWSJob {
	private static final long serialVersionUID = 1L;

	/** Name of the standard TAP parameter which specifies the type of request to execute: "REQUEST". */
	public static final String PARAM_REQUEST = "request";
	/** REQUEST value meaning an ADQL query must be executed: "doQuery". */
	public static final String REQUEST_DO_QUERY = "doQuery";
	/** REQUEST value meaning VO service capabilities must be returned: "getCapabilities". */
	public static final String REQUEST_GET_CAPABILITIES = "getCapabilities";

	/** Name of the standard TAP parameter which specifies the query language: "LANG". <i>(only the ADQL language is supported by default in this version of the library)</i> */
	public static final String PARAM_LANGUAGE = "lang";
	/** LANG value meaning ADQL language: "ADQL". */
	public static final String LANG_ADQL = "ADQL";
	/** LANG value meaning PQL language: "PQL". <i>(this language is not supported in this version of the library)</i> */
	public static final String LANG_PQL = "PQL";

	/** Name of the standard TAP parameter which specifies the version of the TAP protocol that must be used: "VERSION". <i>(only the version 1.0 is supported in this version of the library)</i> */
	public static final String PARAM_VERSION = "version";
	/** VERSION value meaning the version 1.0 of TAP: "1.0". */
	public static final String VERSION_1_0 = "1.0";

	/** Name of the standard TAP parameter which specifies the output format (format of a query result): "FORMAT". */
	public static final String PARAM_FORMAT = "format";
	/** FORMAT value meaning the VOTable format: "votable". */
	public static final String FORMAT_VOTABLE = "votable";

	/** Name of the standard TAP parameter which specifies the maximum number of rows that must be returned in the query result: "MAXREC". */
	public static final String PARAM_MAX_REC = "maxRec";
	/** Special MAXREC value meaning the number of output rows is not limited. */
	public static final int UNLIMITED_MAX_REC = -1;

	/** Name of the standard TAP parameter which specifies the query to execute: "QUERY". */
	public static final String PARAM_QUERY = "query";

	/** Name of the standard TAP parameter which defines the tables to upload in the database for the query execution: "UPLOAD". */
	public static final String PARAM_UPLOAD = "upload";

	/** Name of the library parameter which informs about a query execution progression: "PROGRESSION". <i>(this parameter is removed once the execution is finished)</i> */
	public static final String PARAM_PROGRESSION = "progression";

	/** Internal query execution report. */
	protected TAPExecutionReport execReport = null;

	/** Parameters of this job for its execution. */
	protected final TAPParameters tapParams;

	/**
	 * <p>Build a pending TAP job with the given parameters.</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link #PARAM_PHASE} (</i>phase<i>) is given with the value {@link #PHASE_RUN}
	 * the job execution starts immediately after the job has been added to a job list or after {@link #applyPhaseParam(JobOwner)} is called.</i></p>
	 * 
	 * @param owner		User who owns this job. <i>MAY BE NULL</i>
	 * @param tapParams	Set of parameters.
	 * 
	 * @throws TAPException	If one of the given parameters has a forbidden or wrong value.
	 */
	public TAPJob(final JobOwner owner, final TAPParameters tapParams) throws TAPException{
		super(owner, tapParams);
		this.tapParams = tapParams;
		tapParams.check();
	}

	/**
	 * <p>Restore a job in a state defined by the given parameters.
	 * The phase must be set separately with {@link #setPhase(uws.job.ExecutionPhase, boolean)}, where the second parameter is true.</p>
	 * 
	 * @param jobID		ID of the job.
	 * @param owner		User who owns this job.
	 * @param params	Set of not-standard UWS parameters (i.e. what is called by {@link UWSJob} as additional parameters ; they includes all TAP parameters).
	 * @param quote		Quote of this job.
	 * @param startTime	Date/Time at which this job started. <i>(if not null, it means the job execution was finished, so a endTime should be provided)</i>
	 * @param endTime	Date/Time at which this job finished.
	 * @param results	List of results. <i>NULL if the job has not been executed, has been aborted or finished with an error.</i>
	 * @param error		Error with which this job ends.
	 * 
	 * @throws TAPException	If one of the given parameters has a forbidden or wrong value.
	 */
	public TAPJob(final String jobID, final JobOwner owner, final TAPParameters params, final long quote, final long startTime, final long endTime, final List<Result> results, final ErrorSummary error) throws TAPException{
		super(jobID, owner, params, quote, startTime, endTime, results, error);
		this.tapParams = params;
		this.tapParams.check();
	}

	/**
	 * Get the object storing and managing the set of all (UWS and TAP) parameters.
	 * 
	 * @return The object managing all job parameters.
	 */
	public final TAPParameters getTapParams(){
		return tapParams;
	}

	/**
	 * <p>Get the value of the REQUEST parameter.</p>
	 * 
	 * <p>This value must be {@value #REQUEST_DO_QUERY}.</p>
	 * 
	 * @return	REQUEST value.
	 */
	public final String getRequest(){
		return tapParams.getRequest();
	}

	/**
	 * Get the value of the FORMAT parameter.
	 * 
	 * @return	FORMAT value.
	 */
	public final String getFormat(){
		return tapParams.getFormat();
	}

	/**
	 * <p>Get the value of the LANG parameter.</p>
	 * 
	 * <p>This value should always be {@value #LANG_ADQL} in this version of the library</p>
	 * 
	 * @return	LANG value.
	 */
	public final String getLanguage(){
		return tapParams.getLang();
	}

	/**
	 * <p>Get the value of the MAXREC parameter.</p>
	 * 
	 * <p>If this value is negative, it means the number of output rows is not limited.</p>
	 * 
	 * @return	MAXREC value.
	 */
	public final int getMaxRec(){
		return tapParams.getMaxRec();
	}

	/**
	 * Get the value of the QUERY parameter (i.e. the query, in the language returned by {@link #getLanguage()}, to execute).
	 * 
	 * @return	QUERY value.
	 */
	public final String getQuery(){
		return tapParams.getQuery();
	}

	/**
	 * <p>Get the value of the VERSION parameter.</p>
	 * 
	 * <p>This value should be {@value #VERSION_1_0} in this version of the library.</p>
	 * 
	 * @return	VERSION value.
	 */
	public final String getVersion(){
		return tapParams.getVersion();
	}

	/**
	 * <p>Get the value of the UPLOAD parameter.</p>
	 * 
	 * <p>This value must be formatted as specified by the TAP standard (= a semicolon separated list of DALI uploads).</p>
	 * 
	 * @return	UPLOAD value.
	 */
	public final String getUpload(){
		return tapParams.getUpload();
	}

	/**
	 * <p>Get the list of tables to upload in the database for the query execution.</p>
	 * 
	 * <p>The returned array is an interpretation of the UPLOAD parameter.</p>
	 * 
	 * @return	List of tables to upload.
	 */
	public final DALIUpload[] getTablesToUpload(){
		return tapParams.getUploadedTables();
	}

	/**
	 * <p>Get the execution report.</p>
	 * 
	 * <p>
	 * 	This report is available only during or after the job execution.
	 * 	It tells in which step the execution is, and how long was the previous steps.
	 * 	It can also give more information about the number of resulting rows and columns.
	 * </p>
	 * 
	 * @return The execReport.
	 */
	public final TAPExecutionReport getExecReport(){
		return execReport;
	}

	/**
	 * <p>Set the execution report.</p>
	 * 
	 * <p><b>IMPORTANT:
	 * 	This function can be called only if the job is running or is being restored, otherwise an exception would be thrown.
	 * 	It should not be used by implementors, but only by the internal library processing.
	 * </b></p>
	 * 
	 * @param execReport	An execution report.
	 * 
	 * @throws UWSException	If this job has never been restored and is not running.
	 */
	public final void setExecReport(final TAPExecutionReport execReport) throws UWSException{
		if (getRestorationDate() == null && !isRunning())
			throw new UWSException("Impossible to set an execution report if the job is not in the EXECUTING phase ! Here, the job \"" + jobId + "\" is in the phase " + getPhase());
		this.execReport = execReport;
	}

}
