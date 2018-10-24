package com.vrondakis.zap;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;

/**
 * ZapAction Used by jenkins to add the sidebar button
 */

public class ZapAction implements Action, RunAction2, SimpleBuildStep.LastBuildAction {
    private final Run<?, ?> run;
    private transient ZapTrendChart zapTrendChart;
    private boolean showButton;

    public ZapAction(Run<?, ?> run, boolean showButton) {
        this.run = run;
        this.showButton = showButton;
    }

    public Run<?, ?> getRun() {
        return this.run;
    }

    public String getUrlName() {
        return ZapArchive.DIRECTORY_NAME;
    }

    // Called by Jenkins
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", ""); // Allow JS scripts to be run (content security policy)

        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), this.getTitle(),
                "/plugin/zap-pipeline/logo.png", false);

        dbs.generateResponse(req, rsp, this);
    }

    private void setOwner(Run<?, ?> owner) {
        this.zapTrendChart = new ZapTrendChart(owner.getParent());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(this);
        if (zapTrendChart != null) actions.add(this.zapTrendChart);

        return actions;
    }

    public void doGraph(StaplerRequest req, StaplerResponse res) throws IOException {
        doGraph(req, res, 500, 200);
    }

    public void doGraphLarge(StaplerRequest req, StaplerResponse res) throws IOException {
        doGraph(req, res, 1000, 400);
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp, int width, int height) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // Problem with rendering the chart
            return;
        }

        CategoryDataset dataset = buildDataSet();
        new Graph(-1, width, height) {
            @Override
            protected JFreeChart createGraph() {
                JFreeChart chart = ChartFactory.createLineChart(
                    null, 
                    "Build number", 
                    "Alert instances", 
                    dataset, 
                    PlotOrientation.VERTICAL, 
                    true, 
                    false, 
                    false
                );
      
                chart.setBackgroundPaint(Color.white);

                CategoryPlot plot = chart.getCategoryPlot();
                CategoryItemRenderer renderer = plot.getRenderer();

                BasicStroke stroke = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                renderer.setBaseStroke(stroke);

                renderer.setSeriesPaint(0, new Color(213, 76, 83));
                renderer.setSeriesPaint(1, new Color(245, 166, 35));
                renderer.setSeriesPaint(2, new Color(74, 144, 226));
                renderer.setSeriesPaint(3, new Color(41, 41, 41));

                BasicStroke smallStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                renderer.setSeriesStroke(3, smallStroke);

                plot.setBackgroundPaint(Color.white);
                plot.setOutlinePaint(null);
                plot.setRangeGridlinesVisible(true);
                plot.setRangeGridlinePaint(Color.black);

                CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
                domainAxis.setMaximumCategoryLabelWidthRatio(1.0f);

                LegendTitle legend = chart.getLegend();
                legend.setPosition(RectangleEdge.BOTTOM);

                plot.setDomainAxis(domainAxis);
                domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
                domainAxis.setLowerMargin(0);
                domainAxis.setUpperMargin(0);
                domainAxis.setCategoryMargin(0);

                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                rangeAxis.setLowerBound(0);

                return chart;
            }
        }.doPng(req, rsp);
    }

    private CategoryDataset buildDataSet() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TreeMap<Integer, ZapAlertCount> countAlerts = zapTrendChart.getAlertCounts(run);

        countAlerts.forEach((k, v) -> {
            dataset.addValue(v.getHighAlerts(), "High", v.getBuildName());
            dataset.addValue(v.getMediumAlerts(), "Medium", v.getBuildName());
            dataset.addValue(v.getLowAlerts(), "Low", v.getBuildName());
            dataset.addValue(v.getFalsePositives(), "False positives", v.getBuildName());
        });

        return dataset;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        setOwner(run);
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        setOwner(run);
    }

    @Override
    public String getDisplayName() {
        return showButton ? "ZAP Scanning Report" : null;
    }

    @Override
    public String getIconFileName() {
        return showButton ? "/plugin/zap-pipeline/logo.png" : null;
    }

    protected String getTitle() {
        return this.run.getDisplayName();
    }

    private File dir() {
        return new File(run.getRootDir(), ZapArchive.DIRECTORY_NAME);
    }
}
