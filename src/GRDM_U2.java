import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GRDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	//IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
    	IJ.open("C:\\Users\\to0o\\GLDM2\\src\\orchid.jpg");
		
		GRDM_U2 pw = new GRDM_U2();
		pw.imp = IJ.getImage();
		pw.run("");
	}
    
    public void run(String arg) {
    	if (imp==null) 
    		imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);
        
        storePixelValues(imp.getProcessor());
        
        new CustomWindow(imp, cc);
    }


    private void storePixelValues(ImageProcessor ip) {
    	width = ip.getWidth();
		height = ip.getHeight();
		
		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {
    
        CustomCanvas(ImagePlus imp) {
            super(imp);
        }
    
    } // CustomCanvas inner class
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderSaturation;
		private JSlider jSliderHue;
		private double brightness;
		private double contrast = 1;
		private double saturation = 1;
		private double hue;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 256, 128);
            jSliderContrast = makeTitledSilder("Kontrast", 0, 200, 100);
			jSliderSaturation = makeTitledSilder("Sättigung", 0, 200, 100);
			jSliderHue = makeTitledSilder("Farbwert", 0, 360, 180);
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
			panel.add(jSliderSaturation);
			panel.add(jSliderHue);
            
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue()-128;
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) {
				double value = slider.getValue();
				int halfSlider = (int)(double)(slider.getMaximum()-slider.getMinimum())/2;
				if (value <= halfSlider) {
					contrast = value/halfSlider;
				} else {
					contrast = 1+(value-halfSlider)/10;
				}
				String str = "Kontrast " + contrast;
				setSliderTitle(jSliderContrast, str);
			}

			if (slider == jSliderSaturation) {
				double value = slider.getValue();
				if (value <= 100) {
					saturation = value / 100;
				} else {
					saturation = 1+(value-100)/20;
				}
				String str = "Sättigung " + saturation;
				setSliderTitle(jSliderSaturation, str);
			}

			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Farbwert " + hue;
				setSliderTitle(jSliderHue, str);
			}
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			//int check1 = 0;
			//int check2 = 0;
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;

					//check1 = check1 + r + g + b;

					double luminanz = 0.299 *  r + 0.587 *  g + 0.114 *  b;
					double u = (b - luminanz) * 0.493;
					double v = (r - luminanz) * 0.877;


					//check2 = check2 + r + g + b;
					// anstelle dieser drei Zeilen später hier die Farbtransformation durchführen,
					// die Y Cb Cr -Werte verändern und dann wieder zurücktransformieren
					luminanz = (128 + (luminanz - 128) * contrast) + brightness;
					u = (u * contrast * saturation);
					v = (v * contrast * saturation);




					int rn = (int) Math.round(luminanz + v / 0.877);
					int bn = (int) Math.round(luminanz + u / 0.493);
					int gn = (int) Math.round(1 / 0.587 * luminanz - 0.299 / 0.587 * rn - 0.114 / 0.587 * bn);

					if (rn > 255) {
						rn = 255;
					}
					if (gn > 255) {
						gn = 255;
					}
					if (bn > 255) {
						bn = 255;
					}
					if (rn < 0) {
						rn = 0;
					}
					if (gn < 0) {
						gn = 0;
					}
					if (bn < 0) {
						bn = 0;
					}
					
					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
			//System.out.println(check1+" = "+check2);
		}
	} // CustomWindow inner class
} 
