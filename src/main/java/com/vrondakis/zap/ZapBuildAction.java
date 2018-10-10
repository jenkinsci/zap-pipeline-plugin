package com.vrondakis.zap;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import hudson.util.Area;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StrokeMap;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Zap build action Sets up the sidebar Jenkins buttons
 */

@SuppressWarnings("ALL")
public abstract class ZapBuildAction implements Action, RunAction2, SimpleBuildStep.LastBuildAction {
    private final Run<?, ?> run;
    private transient ZapTrendChart zapTrendChart;

    ZapBuildAction(Run<?, ?> run) {
        this.run = run;
    }

    public Run<?, ?> getRun() {
        return this.run;
    }

    public String getIconFileName() {
        return "/plugin/zap-jenkins-plugin/logo.png";
    }

    public String getDisplayName() {
        return "ZAP Scanning Report";
    }

    public String getUrlName() {
        return Constants.DIRECTORY_NAME;
    }


    // Called by Jenkins
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", ""); // Allow JS scripts to be run (content security policy)

        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), this.getTitle(),
                "/plugin/zap-jenkins-plugin/logo.png", false);

        if (req.getRestOfPath().equals("graph")) {
            doGraph(req, rsp);
            return;
        }

        dbs.generateResponse(req, rsp, this);
    }

    private void setOwner(Run<?, ?> owner) {
        this.zapTrendChart = new ZapTrendChart(owner.getParent());
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        setOwner(run);
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        setOwner(run);
    }

    protected abstract String getTitle();

    @Override
    public Collection<? extends Action> getProjectActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(this);
        if (zapTrendChart != null) actions.add(this.zapTrendChart);

        return actions;
    }

    protected File dir() {
        return null;
    }


    public void doTrend(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        doGraph(req, rsp);
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // Problem with rendering the chart
            return;
        }

        CategoryDataset dataset = buildDataSet();
        new Graph(-1, 500, 200) {
            @Override
            protected JFreeChart createGraph() {
                JFreeChart chart = ChartFactory.createLineChart(
                        "ZAP security scanning", "Build number", "ZAP alert instances", dataset, PlotOrientation.VERTICAL, true, false, false);
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
                domainAxis.setCategoryMargin(20);

                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                rangeAxis.setLowerBound(0);

                return chart;
            }
        }.doPng(req, rsp);
    }

    private CategoryDataset buildDataSet() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        TreeMap<Integer, ZapAlertCount> countAlerts = new TreeMap<Integer, ZapAlertCount>(zapTrendChart.getAlertCounts(run));

        countAlerts.forEach((k, v) -> {
            if(!(v.highAlerts <= 0 && v.mediumAlerts <= 0 && v.lowAlerts <= 0 && v.falsePositives <= 0)) {
                dataset.addValue(v.highAlerts, "High", v.buildName);
                dataset.addValue(v.mediumAlerts, "Medium", v.buildName);
                dataset.addValue(v.lowAlerts, "Low", v.buildName);
                dataset.addValue(v.falsePositives, "False positives", v.buildName);
            }
        });


        return dataset;
    }
}