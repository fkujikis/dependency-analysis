package org.jboss.da.rest.reports;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.FindGAVDependencyException;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.listings.api.model.ProductVersion;
import org.jboss.da.listings.api.service.BlackArtifactService;
import org.jboss.da.listings.api.service.ProductVersionService;
import org.jboss.da.listings.model.rest.RestGavProducts;
import org.jboss.da.listings.model.rest.RestProductInput;
import org.jboss.da.model.rest.ErrorMessage;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.api.AdvancedArtifactReport;
import org.jboss.da.reports.api.ArtifactReport;
import org.jboss.da.reports.api.ReportsGenerator;
import org.jboss.da.reports.api.SCMLocator;
import org.jboss.da.reports.api.VersionLookupResult;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.jboss.da.reports.model.rest.AdvancedReport;
import org.jboss.da.reports.model.rest.AlignReport;
import org.jboss.da.reports.model.rest.AlignReportRequest;
import org.jboss.da.reports.model.rest.BuiltReport;
import org.jboss.da.reports.model.rest.BuiltReportRequest;
import org.jboss.da.reports.model.rest.GAVAvailableVersions;
import org.jboss.da.reports.model.rest.GAVBestMatchVersion;
import org.jboss.da.reports.model.rest.LookupReport;
import org.jboss.da.reports.model.rest.Report;
import org.jboss.da.rest.facade.ReportsFacade;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Main end point for the reports
 * 
 * @author Dustin Kut Moy Cheung
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Path("/reports")
@Api(value = "reports")
public class Reports {

    @Inject
    private Logger log;

    @Inject
    private VersionFinder versionFinder;

    @Inject
    private ReportsGenerator reportsGenerator;

    @Inject
    private ProductVersionService productVersionService;

    @Inject
    private BlackArtifactService blackArtifactService;

    @Inject
    private ReportsFacade reportsFacade;

    @POST
    @Path("/scm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = Report.class)
    public Response scmGenerator(@ApiParam(value = "scm information") SCMLocator scm) {

        try {

            Optional<ArtifactReport> artifactReport = reportsGenerator.getReportFromSCM(scm);

            return artifactReport
                    .map(x -> Response.ok().entity(toReport(x)).build())
                    .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity(new ErrorMessage(ErrorMessage.eType.NO_RELATIONSHIP_FOUND, "No relationship found")).build());

        } catch (ScmException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT, "Exception thrown in scm endpoint", e.getMessage())).build();
        }
        catch (PomAnalysisException e) {
            log.error("Exception thrown during POM analysis", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS, "Exception thrown during POM analysis", e.getMessage())).build(); 
        }
        catch (IllegalArgumentException e) {
            log.error("Illegal arguments excetion", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.ILLEGAL_ARGUMENTS, "Illegal arguments excetion", e.getMessage())).build();
        }
        catch (CommunicationException e) {
            log.error("Exception during communication", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL, "Exception during communication", e.getMessage())).build();        
        }
    }

    @POST
    @Path("/scm-advanced")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a project specified in a repository URL",
            response = AdvancedReport.class)
    public Response advancedScmGenerator(@ApiParam(value = "scm information") SCMLocator scm) {

        try {

            Optional<AdvancedArtifactReport> advancedArtifactReport = reportsGenerator
                    .getAdvancedReportFromSCM(scm);

            return advancedArtifactReport
                    .map(x -> Response.ok().entity(toAdvancedReport(x)).build())
                    .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity(new ErrorMessage(ErrorMessage.eType.NO_RELATIONSHIP_FOUND, "No relationship found")).build());

        } catch (ScmException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT, "Exception thrown in scm endpoint", e.getMessage())).build();
        }
        catch (PomAnalysisException e) {
            log.error("Exception thrown during POM analysis", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS, "Exception thrown during POM analysis", e.getMessage())).build();
        }
        catch (IllegalArgumentException e) {
            log.error("Illegal arguments excetion", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.ILLEGAL_ARGUMENTS, "Illegal arguments excetion", e.getMessage())).build(); 
        }
        catch (CommunicationException e) {
            log.error("Exception during communication", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL, "Exception during communication", e.getMessage())).build();        
        }
        
    }

    @POST
    @Path("/gav")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get dependency report for a GAV ", response = Report.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Requested GAV was not found in repository",
                    response = ErrorMessage.class),
            @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    public Response gavGenerator(@ApiParam(
            value = "JSON Object with keys 'groupId', 'artifactId', and 'version'") GAV gavRequest) {
        try {
            ArtifactReport artifactReport = reportsGenerator.getReport(gavRequest);
            return Response.ok().entity(toReport(artifactReport)).build();
        } catch (CommunicationException ex) {
            log.error("Communication with remote repository failed", ex);
            return Response
                    .status(502)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL,
                            "Communication with remote repository failed", ex.getMessage()))
                    .build();
        } catch (FindGAVDependencyException ex) {
            log.error("Could not find gav in AProx", ex);
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(new ErrorMessage(ErrorMessage.eType.GA_NOT_FOUND,
                            "Requested GA was not found", ex.getMessage())).build();
        }
    }

    @POST
    @Path("/lookup/gavs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup built versions for the list of provided GAVs",
            responseContainer = "List", response = LookupReport.class)
    @ApiResponses(value = {
            @ApiResponse(code = 502, message = "Communication with remote repository failed") })
    public Response lookupGav(
            @ApiParam(
                    value = "JSON list of objects with keys 'groupId', 'artifactId', and 'version'") List<GAV> gavRequest) {

        // Use a concurrent list since we're using parallel stream to add stuff to the list
        CopyOnWriteArrayList<LookupReport> reportsList = new CopyOnWriteArrayList<>();

        boolean communicationSucceeded = gavRequest.parallelStream().distinct().map((gav) -> {
            try {
                VersionLookupResult lookupResult = versionFinder.lookupBuiltVersions(gav);
                LookupReport lookupReport = toLookupReport(gav, lookupResult);
                reportsList.add(lookupReport);
                return true;
            } catch (CommunicationException ex) {
                log.error("Communication with remote repository failed", ex);
                return false;
            }
        }).allMatch(x -> {
            return x;
        });

        if (!communicationSucceeded)
            return Response.status(502)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL, "Communication with remote repository failed"))
                    .build();
        else
            return Response.status(Status.OK).entity(reportsList).build();
    }

    @POST
    @Path("/align")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get alignment report for project specified in a repository URL.",
            response = AlignReport.class)
    public Response alignReport(AlignReportRequest request) {
        try {
            AlignReport aligmentReport = reportsFacade.alignReport(request);
            return Response.status(Status.OK).entity(aligmentReport).build();
        } catch (ScmException e) {
            log.error("Exception thrown in scm endpoint", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT,
                            "Exception thrown in scm endpoint", e.getMessage())).build();
        } catch (PomAnalysisException e) {
            log.error("Exception thrown during POM analysis", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS,
                            "Exception thrown during POM analysis", e.getMessage())).build();
        }
    }

    @POST
    @Path("/built")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get builded artifacts for project specified in a repository URL.",
            response = BuiltReport.class)
    public Response builtReport(BuiltReportRequest request) {
        try {
            Set<BuiltReport> builtReport = reportsFacade.builtReport(request);
            return Response.status(Status.OK).entity(builtReport).build();
        } catch (ScmException e) {
            log.error("Exception thrown in SCM analysis", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.SCM_ENDPOINT, e.getMessage()))
                    .build();
        } catch (PomAnalysisException e) {
            log.error("Exception thrown in POM analysis", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.POM_ANALYSIS, e.getMessage()))
                    .build();
        } catch (CommunicationException e) {
            log.error("Communication with remote repository failed", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(ErrorMessage.eType.COMMUNICATION_FAIL, e.getMessage()))
                    .build();
        }
    }

    private static Report toReport(ArtifactReport report) {
        List<Report> dependencies = report.getDependencies()
                .stream()
                .map(Reports::toReport)
                .collect(Collectors.toList());

        return new Report(report.getGav(), new ArrayList<>(report.getAvailableVersions()),
                report.getBestMatchVersion().orElse(null), report.isDependencyVersionSatisfied(),
                dependencies,
                report.isBlacklisted(), toWhitelisted(report.getWhitelisted()),
                report.getNotBuiltDependencies());
    }

    private static AdvancedReport toAdvancedReport(AdvancedArtifactReport advancedArtifactReport) {
        Report report = toReport(advancedArtifactReport.getArtifactReport());
        return new AdvancedReport(report, advancedArtifactReport.getBlacklistedArtifacts(),
                toRestGAVProducts(advancedArtifactReport.getWhitelistedArtifacts()),
                toGAVBestMatchVersions(advancedArtifactReport
                        .getCommunityGavsWithBestMatchVersions()),
                toGAVAvailableVersions(advancedArtifactReport.getCommunityGavsWithBuiltVersions()),
                advancedArtifactReport.getCommunityGavs());
    }

    private static Set<GAVBestMatchVersion> toGAVBestMatchVersions(
            Map<GAV, String> bestMatchVersions) {
        return bestMatchVersions.entrySet().stream()
                .map(e -> new GAVBestMatchVersion(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());

    }

    private static Set<GAVAvailableVersions> toGAVAvailableVersions(
            Map<GAV, Set<String>> buildVersions) {
        return buildVersions.entrySet().stream()
                .map(e -> new GAVAvailableVersions(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    private static Set<RestGavProducts> toRestGAVProducts(
            Map<GAV, Set<ProductVersion>> whitelistedArtifacts) {
        return whitelistedArtifacts.entrySet().stream()
                .map(e -> new RestGavProducts(e.getKey(), toRestProductInputs(e.getValue())))
                .collect(Collectors.toSet());
    }

    private static Set<RestProductInput> toRestProductInputs(Set<ProductVersion> product) {
        return product.stream()
                .map(p -> toRestProductInput(p))
                .collect(Collectors.toSet());
    }

    private static RestProductInput toRestProductInput(ProductVersion product) {
        RestProductInput ret = new RestProductInput();
        ret.setName(product.getProduct().getName());
        ret.setVersion(product.getProductVersion());
        ret.setSupportStatus(product.getSupport());
        return ret;
    }

    private LookupReport toLookupReport(GAV gav, VersionLookupResult lookupResult) {
        return new LookupReport(gav, lookupResult.getBestMatchVersion().orElse(null),
                lookupResult.getAvailableVersions(), isBlacklisted(gav),
                toWhitelisted(getWhitelisted(gav)));
    }

    private static List<RestProductInput> toWhitelisted(List<ProductVersion> whitelisted) {
        return whitelisted
                .stream()
                .map(pv -> new RestProductInput(pv.getProduct().getName(), pv.getProductVersion(),
                        pv.getSupport()))
                .collect(Collectors.toList());
    }

    private List<ProductVersion> getWhitelisted(GAV gav) {
        return productVersionService.getProductVersionsOfArtifact(gav.getGroupId(),
                gav.getArtifactId(), gav.getVersion());
    }

    private boolean isBlacklisted(GAV gav) {
        return blackArtifactService.isArtifactPresent(gav.getGroupId(), gav.getArtifactId(),
                gav.getVersion());
    }

}
