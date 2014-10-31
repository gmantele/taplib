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

import java.util.Collection;
import java.util.Iterator;

import tap.file.LocalTAPFileManager;
import tap.file.TAPFileManager;
import tap.formatter.OutputFormat;
import tap.log.DefaultTAPLog;
import tap.log.TAPLog;
import tap.metadata.TAPMetadata;
import uws.service.UserIdentifier;
import adql.db.FunctionDef;

/**
 * <p>Description and parameters list of a TAP service.</p>
 * 
 * <p>
 * 	Through this object, it is possible to configure the different limits and formats,
 * 	but also to list all available tables and columns, to declare geometry features as all allowed user defined functions
 * 	and to say where log and other kinds of files must be stored.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS;ARI)
 * @version 2.0 (10/2014)
 */
public interface ServiceConnection {

	/**
	 * List of possible limit units.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS;ARI)
	 * @version 2.0 (10/2014)
	 */
	public static enum LimitUnit{
		rows("row"), bytes("byte");

		private final String str;

		private LimitUnit(final String str){
			this.str = str;
		}

		@Override
		public String toString(){
			return str;
		}
	}

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Name of the service provider ; it can be an organization as an individual person.</p>
	 * 
	 * <p>There is no restriction on the syntax or on the label to use ; this information is totally free</p>
	 * 
	 * <p>It will be used as additional information (INFO tag) in any VOTable and HTML output.</p>
	 * 
	 * @return	The TAP service provider or NULL to leave this field blank.
	 */
	public String getProviderName();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Description of the service provider.</p>
	 * 
	 * <p>It will be used as additional information (INFO tag) in any VOTable output.</p>
	 * 
	 * @return	The TAP service description or NULL to leave this field blank.
	 */
	public String getProviderDescription();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>This function controls the state of the whole TAP service.</p>
	 * 
	 * <p>
	 * 	A message explaining the current state of the TAP service could be provided thanks to {@link #getAvailability()}.
	 * </p>
	 * 
	 * @return	<i>true</i> to enable all TAP resources, <i>false</i> to disable all of them (except /availability).
	 */
	public boolean isAvailable();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get an explanation about the current TAP service state (working or not).
	 * This message aims to provide more details to the users about the availability of this service,
	 * or more particularly about its unavailability.</p>
	 * 
	 * @return	Explanation about the TAP service state.
	 */
	public String getAvailability();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the limit of the retention period.</p>
	 * 
	 * <p>
	 * 	It is the maximum period while an asynchronous job can leave in the jobs list
	 * 	and so can stay on the server.
	 * </p>
	 * 
	 * <p><b>Important notes:</b></p>
	 * <ul>
	 * 	<li><b>Exactly 2 values or a NULL object is expected here.</b></li>
	 * 	<li><b>If NULL</b>, the retention period is not limited and jobs will
	 * 	    theoretically stay infinitely on the server.</li>
	 * 	<li><b>If not NULL</b>, the 2 values must correspond to the default retention period
	 * 	    and the maximum retention period.</li>
	 * 	<li><b>The default value</b> is used to set the retention period when a job is created with no user defined retention period.</li>
	 * 	<li><b>The maximum value</b> is used to limit the retention period when specified by the user while creating a job.</li>
	 * 	<li><b>The default value</b> MUST be less or equals the maximum value.</li>
	 * 	<li><b>Both values must be positive</b>. If a negative value is given it will be interpreted as "no limit".</li>
	 * </ul>
	 * 
	 * @return NULL if no limit must be set, or a two-items array ([0]: default value, [1]: maximum value).
	 */
	public int[] getRetentionPeriod();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the limit of the job execution duration.</p>
	 * 
	 * <p>
	 * 	It is the duration of a running job (including the query execution).
	 * 	This duration is used for synchronous AND asynchronous jobs.
	 * </p>
	 * 
	 * <p><b>Important notes:</b></p>
	 * <ul>
	 * 	<li><b>Exactly 2 values or a NULL object is expected here.</b></li>
	 * 	<li><b>If NULL</b>, the execution duration is not limited and jobs could
	 * 	    theoretically run infinitely.</li>
	 * 	<li><b>If not NULL</b>, the 2 values must correspond to the default execution duration
	 * 	    and the maximum execution duration.</li>
	 * 	<li><b>The default value</b> is used to set the execution duration when a job is created with no user defined execution duration.</li>
	 * 	<li><b>The maximum value</b> is used to limit the execution duration when specified by the user while creating a job.</li>
	 * 	<li><b>The default value</b> MUST be less or equals the maximum value.</li>
	 * 	<li><b>Both values must be positive</b>. If a negative value is given it will be interpreted as "no limit".</li>
	 * </ul>
	 * 
	 * @return	NULL if no limit must be set, or a two-items array ([0]: default value, [1]: maximum value).
	 */
	public int[] getExecutionDuration();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the limit of the job execution result.</p>
	 * 
	 * <p>
	 * 	This value will limit the size of the query results, either in rows or in bytes.
	 * 	The type of limit is defined by the function {@link #getOutputLimitType()}.
	 * </p>
	 * 
	 * <p><b>Important notes:</b></p>
	 * <ul>
	 * 	<li><b>Exactly 2 values or a NULL object is expected here.</b></li>
	 * 	<li><b>If NULL</b>, the output limit is not limited and jobs could theoretically
	 * 	    return very big files.</li>
	 * 	<li><b>If not NULL</b>, the 2 values must correspond to the default output limit
	 * 	    and the maximum output limit.</li>
	 * 	<li><b>The default value</b> is used to set the output limit when a job is created with no user defined output limit.</li>
	 * 	<li><b>The maximum value</b> is used to limit the output limit when specified by the user while creating a job.</li>
	 * 	<li><b>The structure of the object</b> returned by this function MUST be the same as the object returned by {@link #getOutputLimitType()}.
	 * 	    Particularly, the type given by the N-th item of {@link #getOutputLimitType()} must correspond to the N-th limit returned by this function.</li>
	 * 	<li><b>The default value</b> MUST be less or equals the maximum value.</li>
	 * 	<li><b>Both values must be positive</b>. If a negative value is given it will be interpreted as "no limit".</li>
	 * </ul>
	 * 
	 * <p><i><b>Important note:</b>
	 * 	To save performances, it is strongly recommended to use ROWS limit rather than in bytes. Indeed, the rows limit can be taken
	 * 	into account at the effective execution of the query (so before getting the result), on the contrary of the bytes limit which
	 * 	will be applied on the query result.
	 * </i></p>
	 * 
	 * @return	NULL if no limit must be set, or a two-items array ([0]: default value, [1]: maximum value).
	 * 
	 * @see #getOutputLimitType()
	 */
	public int[] getOutputLimit();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the type of each output limit set by this service connection (and accessible with {@link #getOutputLimit()}).</p>
	 * 
	 * <p><b>Important notes:</b></p>
	 * <ul>
	 * 	<li><b>Exactly 2 values or a NULL object is expected here.</b></li>
	 * 	<li><b>If NULL</b>, the output limit will be considered as expressed in ROWS.</li>
	 * 	<li><b>The structure of the object</b> returned by this function MUST be the same as the object returned by {@link #getOutputLimit()}.
	 * 	    Particularly, the type given by the N-th item of this function must correspond to the N-th limit returned by {@link #getOutputLimit()}.</li>
	 * </ul>
	 * 
	 * <p><i><b>Important note:</b>
	 * 	To save performances, it is strongly recommended to use ROWS limit rather than in bytes. Indeed, the rows limit can be taken
	 * 	into account at the effective execution of the query (so before getting the result), on the contrary of the bytes limit which
	 * 	will be applied on the query result.
	 * </i></p>
	 * 
	 * @return	NULL if limits should be expressed in ROWS, or a two-items array ([0]: type of getOutputLimit()[0], [1]: type of getOutputLimit()[1]). 
	 * 
	 * @see #getOutputLimit()
	 */
	public LimitUnit[] getOutputLimitType();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the object to use in order to identify users at the origin of requests.</p>
	 * 
	 * @return	NULL if no user identification should be done, a {@link UserIdentifier} instance otherwise.
	 */
	public UserIdentifier getUserIdentifier();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>This function let enable or disable the upload capability of this TAP service.</p>
	 * 
	 * <p><i>Note:
	 * 	If the upload is disabled, the request is aborted and an HTTP-400 error is thrown each time some tables are uploaded.
	 * </i></p>
	 * 
	 * @return	<i>true</i> to enable the upload capability, <i>false</i> to disable it.
	 */
	public boolean uploadEnabled();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the maximum size of EACH uploaded table.</p>
	 * 
	 * <p>
	 * 	This value is expressed either in rows or in bytes.
	 * 	The unit limit is defined by the function {@link #getUploadLimitType()}.
	 * </p>
	 * 
	 * <p><b>Important notes:</b></p>
	 * <ul>
	 * 	<li><b>Exactly 2 values or a NULL object is expected here.</b></li>
	 * 	<li><b>If NULL</b>, the upload limit is not limited and uploads could be
	 * 	    theoretically unlimited.</li>
	 * 	<li><b>If not NULL</b>, the 2 values must correspond to the default upload limit
	 * 	    and the maximum upload limit.</li>
	 * 	<li><b>The default value</b> is used inform the user about the server wishes.</li>
	 * 	<li><b>The maximum value</b> is used to really limit the upload limit.</li>
	 * 	<li><b>The structure of the object</b> returned by this function MUST be the same as the object returned by {@link #getUploadLimitType()}.
	 * 	    Particularly, the type given by the N-th item of {@link #getUploadLimitType()} must correspond to the N-th limit returned by this function.</li>
	 * 	<li><b>The default value</b> MUST be less or equals the maximum value.</li>
	 * 	<li><b>Both values must be positive</b>. If a negative value is given it will be interpreted as "no limit".</li>
	 * </ul>
	 * 
	 * <p><i><b>Important note:</b>
	 * 	To save performances, it is recommended to use BYTES limit rather than in rows. Indeed, the bytes limit can be taken
	 * 	into account at directly when reading the bytes of the request, on the contrary of the rows limit which
	 * 	requires to parse the uploaded tables.
	 * </i></p>
	 * 
	 * @return	NULL if no limit must be set, or a two-items array ([0]: default value, [1]: maximum value).
	 * 
	 * @see #getUploadLimitType()
	 */
	public int[] getUploadLimit();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the type of each upload limit set by this service connection (and accessible with {@link #getUploadLimit()}).</p>
	 * 
	 * <p><b>Important notes:</b></p>
	 * <ul>
	 * 	<li><b>Exactly 2 values or a NULL object is expected here.</b></li>
	 * 	<li><b>If NULL</b>, the upload limit will be considered as expressed in ROWS.</li>
	 * 	<li><b>The structure of the object</b> returned by this function MUST be the same as the object returned by {@link #getUploadLimit()}.
	 * 	    Particularly, the type given by the N-th item of this function must correspond to the N-th limit returned by {@link #getUploadLimit()}.</li>
	 * </ul>
	 * 
	 * <p><i><b>Important note:</b>
	 * 	To save performances, it is recommended to use BYTES limit rather than in rows. Indeed, the bytes limit can be taken
	 * 	into account at directly when reading the bytes of the request, on the contrary of the rows limit which
	 * 	requires to parse the uploaded tables.
	 * </i></p>
	 * 
	 * @return	NULL if limits should be expressed in ROWS, or a two-items array ([0]: type of getUploadLimit()[0], [1]: type of getUploadLimit()[1]). 
	 * 
	 * @see #getUploadLimit()
	 */
	public LimitUnit[] getUploadLimitType();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the maximum size of the whole set of all tables uploaded in one request.
	 * This size is expressed in bytes.</p>
	 * 
	 * <p><b>IMPORTANT 1:
	 * 	This value is always used when the upload capability is enabled.
	 * </b></p>
	 * 
	 * <p><b>IMPORTANT 2:
	 * 	The value returned by this function MUST always be positive.
	 * 	A zero or negative value will throw an exception later while
	 * 	reading parameters in a request with some uploaded tables.
	 * </b></p>
	 * 
	 * @return	A positive (&gt;0) value corresponding to the maximum number of bytes of all uploaded tables sent in one request.
	 */
	public int getMaxUploadSize();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>Get the list of all available tables and columns.</p>
	 * 
	 * <p>
	 * 	This object is really important since it lets the library check ADQL queries properly and set the good type
	 * 	and formatting in the query results.
	 * </p>
	 *  
	 * @return	A TAPMetadata object. <b>NULL is not allowed and will throw a grave error at the service initialization.</b>
	 */
	public TAPMetadata getTAPMetadata();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the list of all allowed coordinate systems.</p>
	 * 
	 * <u><b>Special values</b></u>
	 * 
	 * <p>Two special values can be returned by this function:</p>
	 * <ul>
	 * 	<li><b>NULL</b> which means that all coordinate systems are allowed,</li>
	 * 	<li><b>the empty list</b> which means that no coordinate system - except
	 * 	    the default one (which can be reduced to an empty string) - is allowed.</li>
	 * </ul>
	 * 
	 * <u><b>List item syntax</b></u>
	 * 
	 * <p>
	 * 	Each item of this list is a <b>pattern</b> and not a simple coordinate system.
	 * 	Thus each item MUST respect the following syntax:
	 * </p>
	 * <pre>{framePattern} {refposPattern} {flavorPattern}</pre>
	 * <p>
	 * 	Contrary to a coordinate system expression, all these 3 information are required.
	 * 	Each may take 3 kinds of value:
	 * </p>
	 * <ul>
	 * 	<li>a single value (i.e. "ICRS"),</li>
	 * 	<li>a list of values with the syntax <code>({value1}|{value2}|...)</code> (i.e. "(ICRS|FK4)"),</li>
	 * 	<li>a "*" which means that all values are possible.
	 * </ul>
	 * <p>
	 * 	For instance: <code>(ICRS|FK4) HELIOCENTER *</code> is a good syntax,
	 * 	but not <code>ICRS</code> or <code>ICRS HELIOCENTER</code>.
	 * </p>
	 * 
	 * <p><i>Note:
	 * 	Even if not explicitly part of the possible values, the default value of each part (i.e. UNKNOWNFRAME for frame) is always taken into account by the library.
	 * 	Particularly, the empty string will always be allowed even if not explicitly listed in the list returned by this function.
	 * </i></p>
	 * 
	 * @return	NULL to allow ALL coordinate systems, an empty list to allow NO coordinate system,
	 *        	or a list of coordinate system patterns otherwise.
	 * 
	 * @since 2.0
	 */
	public Collection<String> getCoordinateSystems();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the list of all allowed geometrical functions.</p>
	 * 
	 * <u><b>Special values</b></u>
	 * 
	 * <p>Two special values can be returned by this function:</p>
	 * <ul>
	 * 	<li><b>NULL</b> which means that all geometrical functions are allowed,</li>
	 * 	<li><b>the empty list</b> which means that no geometrical functions is allowed.</li>
	 * </ul>
	 * 
	 * <u><b>List item syntax</b></u>
	 * 
	 * <p>
	 * 	Each item of the returned list MUST be a function name (i.e. "CONTAINS", "POINT").
	 * 	It can also be a type of STC region to forbid (i.e. "POSITION", "UNION").
	 * </p>
	 * 
	 * <p>The given names are not case sensitive.</p>
	 * 
	 * @return	NULL to allow ALL geometrical functions, an empty list to allow NO geometrical function,
	 *        	or a list of geometrical function names otherwise.
	 * 
	 * @since 2.0
	 */
	public Collection<String> getGeometries();

	/**
	 * <i>[OPTIONAL]</i>
	 * <p>Get the list of all allowed User Defined Functions (UDFs).</p>
	 * 
	 * <u><b>Special values</b></u>
	 * 
	 * <p>Two special values can be returned by this function:</p>
	 * <ul>
	 * 	<li><b>NULL</b> which means that all unknown functions (which should be UDFs) are allowed,</li>
	 * 	<li><b>the empty list</b> which means that no unknown functions (which should be UDFs) is allowed.</li>
	 * </ul>
	 * 
	 * <u><b>List item syntax</b></u>
	 * 
	 * <p>
	 * 	Each item of the returned list MUST be an instance of {@link FunctionDef}.
	 * </p>
	 * 
	 * @return	NULL to allow ALL unknown functions, an empty list to allow NO unknown function,
	 *        	or a list of user defined functions otherwise.
	 * 
	 * @since 2.0
	 */
	public Collection<FunctionDef> getUDFs();

	/**
	 * <i>[OPTIONAL]</i>
	 * 
	 * <p>Get the maximum number of asynchronous jobs that can run in the same time.</p>
	 * 
	 * <p>A null or negative value means <b>no limit</b> on the number of running asynchronous jobs.</p> 
	 * 
	 * @return	Maximum number of running jobs (&le;0 => no limit).
	 * 
	 * @since 2.0
	 */
	public int getNbMaxAsyncJobs();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>Get the logger to use in the whole service when any error, warning or info happens.</p>
	 * 
	 * <p><b>IMPORTANT:
	 * 	If NULL is returned by this function, grave errors will occur while executing a query or managing an error.
	 * 	It is strongly recommended to provide a logger, even a basic implementation.
	 * </b></p>
	 * 
	 * <p><i>Piece of advice:
	 * 	A default implementation like {@link DefaultTAPLog} would be most of time largely enough.
	 * </i></p>
	 * 
	 * @return	An instance of {@link TAPLog}.
	 */
	public TAPLog getLogger();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>Get the object able to build other objects essentials to configure the TAP service or to run every queries.</p>
	 * 
	 * <p><b>IMPORTANT:
	 * 	If NULL is returned by this function, grave errors will occur while initializing the service.
	 * </b></p>
	 * 
	 * <p><i>Piece of advice:
	 * 	The {@link TAPFactory} is an interface which contains a lot of functions to implement.
	 * 	It is rather recommended to extend {@link AbstractTAPFactory}: just 3 functions ({@link AbstractTAPFactory#countFreeConnections()},
	 * 	{@link AbstractTAPFactory#freeConnection()}, {@link AbstractTAPFactory#getConnection(String)}) will have to be implemented. 
	 * </i></p>
	 * 
	 * @return	An instance of {@link TAPFactory}.
	 * 
	 * @see AbstractTAPFactory
	 */
	public TAPFactory getFactory();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>Get the object in charge of the files management.
	 * This object manages log, error, result and backup files of the whole service.</p>
	 * 
	 * <p><b>IMPORTANT:
	 * 	If NULL is returned by this function, grave errors will occur while initializing the service.
	 * </b></p>
	 * 
	 * <p><i>Piece of advice:
	 * 	The library provides a default implementation of the interface {@link TAPFileManager}:
	 * 	{@link LocalTAPFileManager}, which stores all files on the local file-system.
	 * </i></p>
	 * 
	 * @return	An instance of {@link TAPFileManager}.
	 */
	public TAPFileManager getFileManager();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>Get the list of all available output formats.</p>
	 * 
	 * <p><b>IMPORTANT:</b></p>
	 * <ul>
	 * 	<li>All formats of this list MUST have a different MIME type.</li>
	 * 	<li>At least one item must correspond to the MIME type "votable".</li>
	 * 	<li>If NULL is returned by this function, grave errors will occur while writing the capabilities of this service.</li>
	 * </li>
	 * 
	 * @return	An iterator on the list of all available output formats.
	 */
	public Iterator<OutputFormat> getOutputFormats();

	/**
	 * <i><b>[MANDATORY]</b></i>
	 * <p>Get the output format having the given MIME type (or short MIME type ~ alias).</p>
	 * 
	 * <p><b>IMPORTANT:
	 * 	This function MUST always return an {@link OutputFormat} instance when the MIME type "votable" is given in parameter.
	 * </b></p>
	 * 
	 * @param mimeOrAlias	MIME type or short MIME type of the format to get.
	 * 
	 * @return	The corresponding {@link OutputFormat} or NULL if not found.
	 */
	public OutputFormat getOutputFormat(final String mimeOrAlias);

}
