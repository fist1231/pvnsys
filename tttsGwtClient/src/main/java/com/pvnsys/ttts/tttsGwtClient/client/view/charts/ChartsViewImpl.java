package com.pvnsys.ttts.tttsGwtClient.client.view.charts;

import org.moxieapps.gwt.highcharts.client.Loading;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.Style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.view.common.SimpleView;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;
import com.pvnsys.ttts.tttsGwtClient.shared.common.ListUtils;
import com.pvnsys.ttts.tttsGwtClient.shared.dto.ChartsDTO;
import com.smartgwt.client.widgets.layout.PortalLayout;
import com.smartgwt.client.widgets.layout.Portlet;

public class ChartsViewImpl<T> extends SimpleView implements ChartsView<T> {

	private Controller<T> controller;
	private ChartsDTO data;

	private static ChartsViewUiBinder uiBinder = GWT.create(ChartsViewUiBinder.class);
	
	StockChart chart = null;
	

	@SuppressWarnings("rawtypes")
	@UiTemplate("ChartsView.ui.xml")
	interface ChartsViewUiBinder extends UiBinder<Widget, ChartsViewImpl> {
	}

	@UiField
	VerticalPanel panel;

	@UiField
	Label waitLabel;
	
	public ChartsViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		waitLabel.setVisible(true);
		
	}

	public void setController(Controller<T> controller) {
		this.controller = controller;
	}

	public Widget asWidget() {
		return this;
	}

	private StockChart makeChart() {

		String ticker = ((StockVO)(data.getData().get(0))).getName();

		StockChart chart = new StockChart()
//				.setType(Series.Type.CANDLESTICK)
				.setChartTitleText(ticker)
				.setMarginRight(10)
				.setLoading(
					     new Loading()
					       .setShowDuration(500)
					       .setStyle(
					           new Style()
					              .setColor("red")
					              .setFontSize("16px")
					    	)
					    )
				.showLoading("Please wait ...")
				.setHeight(500);
//				.setRangeSelector(new RangeSelector().setEnabled(true).setInputEnabled(true));
//				.setLegend(
//						new Legend().setLayout(Legend.Layout.VERTICAL)
//								.setAlign(Legend.Align.RIGHT)
//								.setVerticalAlign(Legend.VerticalAlign.TOP)
//								.setX(-10).setY(100).setBorderWidth(0))
//				.setToolTip(new ToolTip().setShared(true).setFormatter(new ToolTipFormatter() {
//					public String format(ToolTipData toolTipData) {
//						return "<b>" + toolTipData.getSeriesName()
//								+ "</b><br/>" + toolTipData.getXAsString()
//								+ ": " + toolTipData.getYAsDouble() + "$";
//					}
//				}));

		
		String[] xAxis = new String[data.getData().size()];
		Number[][] ohlc = new Number[data.getData().size()][];
		Number[][] vol = new Number[data.getData().size()][];
//		double[] close = new double[data.getData().size()];
		Number[][] outMACDChart = new Number[data.getData().size()][];
		Number[][] outMACDSignalChart = new Number[data.getData().size()][];
		Number[][] outMACDHistChart = new Number[data.getData().size()][];
		
		double[] outMacd = ListUtils.toArrayOfDoubles(data.getMacd().getOutMACD());
		double[] outMACDSignal = ListUtils.toArrayOfDoubles(data.getMacd().getOutMACDSignal());
		double[] outMACDHist = ListUtils.toArrayOfDoubles(data.getMacd().getOutMACDHist());
		
		int count = 0;
		for (StockVO stock : data.getData()) {
			
			String dateStr = DateTimeFormat.getFormat("dd-MMM-yy").format(stock.getStockDate());
			xAxis[count] = dateStr;
			ohlc[count] = new Number[] {getTime(dateStr), stock.getOpen(), stock.getHigh(), stock.getLow(), stock.getClose()};
//			ohlc[count] = new Number[] {count, stock.getClose()};
			vol[count] = new Number[] {getTime(dateStr), stock.getVolume()};
			
			outMACDChart[count] = new Number[] {getTime(dateStr), count<data.getMacd().getBegin()?0:outMacd[count-data.getMacd().getBegin()]};
			outMACDSignalChart[count] = new Number[] {getTime(dateStr), count<data.getMacd().getBegin()?0:outMACDSignal[count-data.getMacd().getBegin()]};
			outMACDHistChart[count] = new Number[] {getTime(dateStr), count<data.getMacd().getBegin()?0:outMACDHist[count-data.getMacd().getBegin()]};
			
//			close[count] = stock.getClose();
			count++;
		}

		/*
		 * ================== ta-lib averages =======================
		 */
		
		

//		addMacd(close);
		
		chart.getXAxis().setCategories(xAxis);

//        chart.getXAxis()  
//        .setType(Axis.Type.DATE_TIME)  
//        .setDateTimeLabelFormats(new DateTimeLabelFormats()  
//            .setMonth("%e. %b")  
//            .setYear("%b")  // don't display the dummy year  
//        );  
//
    chart.getYAxis(0).setOption("height", 300).setAxisTitleText("Price $"); 
//        .setMin(0);  
    chart.getYAxis(1).setOption("height", 100).setOption("top", 300)
    .setAxisTitleText("Volume mln").setReversed(false); 


    
		chart.addSeries(chart.createSeries().setType(Series.Type.CANDLESTICK).setName(ticker).setYAxis(0).setPoints(ohlc));
		chart.addSeries(chart.createSeries().setType(Series.Type.COLUMN).setName("Volume").setYAxis(1).setPoints(vol));

		chart.getYAxis(2).setOption("height", 200).setAxisTitleText("MACD").setOption("top", 250); 
	    chart.getYAxis(3).setOption("height", 200).setAxisTitleText("MACD Signal").setOption("top", 250); 
	    chart.getYAxis(4).setOption("height", 200).setAxisTitleText("MACD Hist").setOption("top", 250); 

	    chart.addSeries(chart.createSeries().setType(Series.Type.LINE).setName("MACD").setYAxis(2).setPoints(outMACDChart));
		chart.addSeries(chart.createSeries().setType(Series.Type.LINE).setName("MACD Signal").setYAxis(3).setPoints(outMACDSignalChart));
		chart.addSeries(chart.createSeries().setType(Series.Type.COLUMN).setName("Hist").setYAxis(4).setPoints(outMACDHistChart));

		return chart;
	}

    private long getTime(String date) {  
        return dateTimeFormat.parse(date).getTime();  
    }  
  
    static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd-MMM-yy");  
    
//    private void addMacd(double[] close) {
//		TAServiceAsync taService = GWT.create(TAService.class);
//		
//		Double[] close_d = new Double[close.length];
//		for(int i=0; i<close.length; i++) {
//			close_d[i] = new Double(close[i]);
//		}
//		
//		taService.getMacd(close_d, new AsyncCallback<MacdVO>() {
//			public void onFailure(Throwable e) {
//                Window.alert("Here is the Error in Gwt_a isLoggedIn() check: " + e.getMessage());
//			}
//
//			public void onSuccess(MacdVO macdVO) {
//				if(macdVO != null) {
//					
//					Number[][] outMACDChart = new Number[data.size()][];
//					Number[][] outMACDSignalChart = new Number[data.size()][];
//					Number[][] outMACDHistChart = new Number[data.size()][];
//					
//					int cnt = 0;
//					for (StockVO stock : (List<StockVO>) data) {
//						
//						String dateStr = DateTimeFormat.getFormat("dd-MMM-yy").format(stock.getStockDate());
//						outMACDChart[cnt] = new Number[] {getTime(dateStr), cnt<macdVO.getBegin()?0:macdVO.getOutMACD()[cnt-macdVO.getBegin()]};
//						outMACDSignalChart[cnt] = new Number[] {getTime(dateStr), cnt<macdVO.getBegin()?0:macdVO.getOutMACDSignal()[cnt-macdVO.getBegin()]};
//						outMACDHistChart[cnt] = new Number[] {getTime(dateStr), cnt<macdVO.getBegin()?0:macdVO.getOutMACDHist()[cnt-macdVO.getBegin()]};
//						cnt++;
//					}
//
//					chart.getYAxis(2).setOption("height", 200).setAxisTitleText("MACD").setOption("top", 250); 
//				    chart.getYAxis(3).setOption("height", 200).setAxisTitleText("MACD Signal").setOption("top", 250); 
//				    chart.getYAxis(4).setOption("height", 200).setAxisTitleText("MACD Hist").setOption("top", 250); 
//
//				    chart.addSeries(chart.createSeries().setType(Series.Type.LINE).setName("MACD").setYAxis(2).setPoints(outMACDChart));
//					chart.addSeries(chart.createSeries().setType(Series.Type.LINE).setName("MACD Signal").setYAxis(3).setPoints(outMACDSignalChart));
//					chart.addSeries(chart.createSeries().setType(Series.Type.COLUMN).setName("Hist").setYAxis(4).setPoints(outMACDHistChart));
//
//					
//					panel.add(chart);
//					waitLabel.setVisible(false);
//				}
//			}
//		});
//    	
//    }

	@Override
	public void setData(ChartsDTO chartsDto) {
		data = chartsDto;
		chart = makeChart();
		PortalLayout portalLayout = new PortalLayout(1);
        portalLayout.setWidth100();  
        portalLayout.setHeight("600px");  
//        portalLayout.setHeight100();  
          
        final Portlet portlet1 = new Portlet();  
        portlet1.setTitle("Chart");
//        portlet1.setAutoSize(true);
//        portlet1.addMaximizeClickHandler(new MaximizeClickHandler() {
//			
//			@Override
//			public void onMaximizeClick(MaximizeClickEvent event) {
//		        portlet1.setAutoSize(true);
//		        portlet1.redraw();
//				
//			}
//		});
//        portlet1.addChild(chart);
        
        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");
        chart.setSize("100%", "100%");
        vp.add(chart);
        
        portlet1.addItem(vp);
//        portlet1.addMember(vp);
//        portlet1.addChild(vp);
//        portlet1.draw();
        
        portalLayout.addPortlet(portlet1, 0, 0);  
		
		panel.add(portalLayout);
//		panel.add(chart);
		waitLabel.setVisible(false);
	}
    
}
