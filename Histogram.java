import java.awt.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import org.jfree.ui.*;
import javax.swing.JFrame;

/**
 * A simple demonstration application showing how to create a bar chart.
 *
 */
public class Histogram extends ApplicationFrame {

    /**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     */
    public Histogram(final String title,double[][] sim) {

        super(title);

        final CategoryDataset dataset = createDataset(sim);
        final JFreeChart chart = createChart(title,dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        JFrame f = new JFrame(title);
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartPanel.setPreferredSize(new Dimension(800, 600));
		f.setLayout(new BorderLayout(800, 600));
        f.add(chartPanel, BorderLayout.CENTER);	
        f.pack();
        f.setLocationRelativeTo(null);
		f.getContentPane().setBackground(new Color(101,67,33));
        f.setVisible(true);
    }

    /**
     * Returns a sample dataset.
     * 
     * @return The dataset.
     */
    private CategoryDataset createDataset(double[][] sim) {
        
        // create the dataset...
		return DatasetUtilities.createCategoryDataset("","",sim);

        
    }
    
    /**
     * Creates a sample chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(String title,final CategoryDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            title,         				// chart title
            "",               			// domain axis label
            "Similiarity",              // range axis label
            dataset,                  	// data
            PlotOrientation.VERTICAL, 	// orientation
            true,                     	// include legend
            true,                     	// tooltips
            false                     	// URLs?
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
        
        // set up gradient paints for series...
        final GradientPaint gp0 = new GradientPaint(
            0.0f, 0.0f, Color.blue, 
            0.0f, 0.0f, Color.white
        );
        final GradientPaint gp1 = new GradientPaint(
            0.0f, 0.0f, Color.blue.darker(), 
            0.0f, 0.0f, Color.white
        );
        final GradientPaint gp2 = new GradientPaint(
            0.0f, 0.0f, Color.blue.brighter(), 
            0.0f, 0.0f, Color.white
        );
        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        renderer.setSeriesPaint(2, gp2);
        renderer.setSeriesPaint(3, gp0);
        renderer.setSeriesPaint(4, gp1);
        renderer.setSeriesPaint(5, gp2);
		renderer.setSeriesPaint(6, gp0);
        renderer.setSeriesPaint(7, gp1);
        renderer.setSeriesPaint(8, gp2);
		renderer.setSeriesPaint(9, gp0);
		renderer.setSeriesPaint(10, gp1);
		
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        // OPTIONAL CUSTOMISATION COMPLETED.
        
        return chart;
        
    }
    
    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    /*public static void main(final String[] args) {

        final Histogram demo = new Histogram("Bar Chart Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }*/

}