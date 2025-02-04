import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.zip.*;
import java.lang.Math.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Displayer
{
	BufferedImage original_image;
	BufferedImage image;
	ImageCanvas   image_canvas;
	
	int           x, y;
	int           image_x_offset, image_y_offset;
	int           canvas_x_offset, canvas_y_offset;
	int           image_xdim, image_ydim;
	int           scaled_xdim, scaled_ydim;
	int           canvas_xdim, canvas_ydim;
	
	int           width, height;
	
	int []        pixel;

	double        scale, xposition, yposition;
	
	JScrollBar    xscrollbar, yscrollbar;
	
	boolean       mouse_changing_scrollbar = false;
	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java Displayer <filename>");
			System.exit(0);
		}
		
		String prefix       = new String("");
		//String prefix       = new String("C:/Users/Brian Crowley/Desktop/");
		String filename     = new String(args[0]);
		
		Displayer writer = new Displayer(prefix + filename);
	}

	public Displayer(String filename)
	{
		try
		{
			File file = new File(filename);
			original_image = ImageIO.read(file);
			image_xdim = original_image.getWidth();
			image_ydim = original_image.getHeight();
			
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			width  = (int)screenSize.getWidth();
			height = (int)screenSize.getHeight();
		
			System.out.println("Xdim = " + image_xdim + ", ydim = " + image_ydim);
			// This is less resolution than the system settings.
			System.out.println("Screen width is " + width + ", screen height is " + height);
			System.out.println();
		  
			JFrame frame = new JFrame("Displaying " + filename);
			    
			WindowAdapter window_handler = new WindowAdapter()
			{
			    public void windowClosing(WindowEvent event)
			    {
			        System.exit(0);
			    }
			};
			frame.addWindowListener(window_handler);
			    
			image_canvas = new ImageCanvas();
				
				
			if(image_xdim <= (width - 20) && image_ydim <= (height - 60))
			{
				canvas_xdim = image_xdim;
				canvas_ydim = image_ydim;
				scale       = 1.;
			}
			else if(image_xdim <= (width - 20))
			{
				canvas_ydim = height - 60; 
				scale       = canvas_ydim;
				scale      /= image_ydim;
				canvas_xdim = (int)(scale * image_xdim);
			}
			else if(image_ydim <= (height - 60))
            {
				canvas_xdim = width - 20; 
				scale       = canvas_ydim;
				scale      /= image_ydim;
            }
			else
			{
				double xscale = width - 20;
				xscale       /= image_xdim;
				double yscale = height - 60;
				yscale       /= image_ydim;
				    
				if(xscale <= yscale)
				    scale = xscale;
				else
				    scale = yscale;
				    
				canvas_xdim = (int)(scale * image_xdim);
				canvas_ydim = (int)(scale * image_ydim);
			}
			
			
			image_canvas.setSize(canvas_xdim, canvas_ydim);
			
			
			
			JScrollBar xscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 49, 1, 0, 98);
			AdjustmentListener xhandler = new AdjustmentListener()
			{
			    public void adjustmentValueChanged(AdjustmentEvent event)
				{
			    	boolean still_changing = event.getValueIsAdjusting();
			    	if(still_changing == false)
			    	{
				        int position = event.getValue();
				        System.out.println("Position of xscrollbar is now " + position);
			    	}
				}
			};		
			xscrollbar.addAdjustmentListener(xhandler);
			
			JScrollBar yscrollbar = new JScrollBar(JScrollBar.VERTICAL, 49, 1, 0, 98);
			AdjustmentListener yhandler = new AdjustmentListener()
			{
			    public void adjustmentValueChanged(AdjustmentEvent event)
				{
			    	boolean still_changing = event.getValueIsAdjusting();
			    	if(still_changing == false)
			    	{
				        int position = event.getValue();
				        System.out.println("Position of yscrollbar is now " + position);
			    	}
				}
			};		
			yscrollbar.addAdjustmentListener(yhandler);
			
			
			if(scaled_xdim <= canvas_xdim)
				xscrollbar.setEnabled(false);
			if(scaled_ydim <= canvas_ydim)
				yscrollbar.setEnabled(false);
		
			
			MouseAdapter zoom_handler = new MouseAdapter()
			{
				public void mouseClicked(MouseEvent event)
				{
					/*
					int    previous_x         = x;
					int    previous_y         = y;
					int    value;
					
					x          = event.getX();
					y          = event.getY();
					
					double x_delta = x - previous_x;
					int    x_range = scaled_xdim - canvas_xdim;
					image_x_offset = (int)(x_range - (1. - xposition) * x_range);
					if(image_x_offset + x_delta < 0)
					{
					    x              = canvas_xdim / 2;
					    image_x_offset = 0;
					    value          = 0;
					    xposition      = 0.;
					}
					else if(image_x_offset + x_delta > x_range)
					{
						x              = canvas_xdim / 2;
					    image_x_offset = x_range;
					    value          = 98;
					    xposition      = 1.;	
					}
					else
					{
						x              = canvas_xdim / 2;
					    image_x_offset += x_delta;
					    xposition       = image_x_offset / x_range;
					    value           = (int)(98. * xposition);
					}
					xscrollbar.setValue(value);
					
					double y_delta = y - previous_y;
					int    y_range = scaled_ydim - canvas_ydim;
					image_y_offset = (int)(y_range - (1. - yposition) * y_range);
					if(image_y_offset + y_delta < 0)
					{
					    y              = canvas_ydim / 2;
					    image_y_offset = 0;
					    value          = 0;
					    yposition      = 0.;
					}
					else if(image_y_offset + y_delta > y_range)
					{
						y              = canvas_ydim / 2;
					    image_y_offset = y_range;
					    value          = 98;
					    yposition      = 1.;	
					}
					else
					{
						y = canvas_ydim / 2;
					    image_y_offset += y_delta;
					    yposition       = image_y_offset / y_range;
					    value           = (int)(98. * yposition);
					}
					yscrollbar.setValue(value);
					*/
					
					int button = event.getButton();
					if(button == 1)
					{
						System.out.println("Zooming in at x = " + x + ", y = " + y);
						scale *= 1.1;
						
						
						if(scale * image_xdim > canvas_xdim && !xscrollbar.isEnabled())
						{
						    xscrollbar.setEnabled(true);   	
						}
						if(scale * image_ydim > canvas_ydim && !yscrollbar.isEnabled())
						{
							yscrollbar.setEnabled(true); 	
						}
					}
					else if (button == 3)
					{
						System.out.println("Zooming out at x = " + x + ", y = " + y);
						scale *= 0.9;
						
						if(scale * image_xdim > canvas_xdim && xscrollbar.isEnabled())
						{
						    xscrollbar.setEnabled(false);   	
						}
						if(scale * image_ydim > canvas_ydim && yscrollbar.isEnabled())
						{
							yscrollbar.setEnabled(false); 	
						}
					}
					image_canvas.repaint();
				}	
			};
			
			image_canvas.addMouseListener(zoom_handler);
			
			
			MouseAdapter move_handler = new MouseAdapter()
			{
				int start_x, end_x;
				int start_y, end_y;
			
				public void mousePressed(MouseEvent event)
				{
					start_x  = event.getX();
					start_y  = event.getY();	
				}
				
				public void mouseReleased(MouseEvent event)
				{
					end_x  = event.getX();
					end_y  = event.getY();	
					
					if(end_x != start_x || end_y != start_y)
					{
						System.out.println("Moved from x = " + start_x + ", y = " + start_y + " to x = " + end_x + ", y = " + end_y);
					}
				}
			};
			
			image_canvas.addMouseListener(move_handler);
			
			x               = canvas_xdim / 2;
			y               = canvas_ydim / 2;
			image_x_offset  = 0;
			image_y_offset  = 0;
			canvas_x_offset = 0;
			canvas_y_offset = 0;
			xposition       = .5;
			yposition       = .5;
			
			JPanel image_panel = new JPanel(new BorderLayout());
			
			image_panel.add(image_canvas, BorderLayout.CENTER);
			image_panel.add(xscrollbar, BorderLayout.SOUTH);
			image_panel.add(yscrollbar, BorderLayout.EAST);

			
			frame.getContentPane().add(image_panel);
			frame.pack();
			frame.setLocation(10, 10);
			frame.setSize(canvas_xdim, canvas_ydim);
			frame.setVisible(true);	
			 
		} 
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	class ImageCanvas extends Canvas
    {
        public synchronized void paint(Graphics g)
        {
        	if(scale == 1.)
			{
        		scaled_xdim = image_xdim;
        		scaled_ydim = image_ydim;
				if(image_xdim <= canvas_xdim && image_ydim <= canvas_ydim)
				{
					image = original_image;
					
					if(image_xdim == canvas_xdim)
						canvas_x_offset = 0;
					else
					{
						int delta       = canvas_xdim - image_xdim;
						canvas_x_offset = delta / 2;
					}
					
					if(image_ydim == canvas_ydim)
						canvas_y_offset = 0;
					else
					{
						int delta       = canvas_ydim - image_ydim;
						canvas_y_offset = delta / 2;
					}
				}
				else
				{
					int upper_x     = x - canvas_xdim / 2;
					int upper_y     = y - canvas_ydim / 2;
					image           = original_image.getSubimage(upper_x + image_x_offset, upper_y + image_y_offset, canvas_xdim, canvas_ydim);
					canvas_x_offset = 0;
					canvas_y_offset = 0;
				}
			}
			else
			{
				System.out.println("Scale is " + String.format("%.4f", scale));
				System.out.println();
				scaled_xdim = (int)(scale * image_xdim);
				scaled_ydim = (int)(scale * image_ydim);
				
				AffineTransform scaling_transform = new AffineTransform();
				scaling_transform.scale(scale, scale);
				AffineTransformOp scale_op = new AffineTransformOp(scaling_transform, AffineTransformOp.TYPE_BILINEAR);
				BufferedImage scaled_image = new BufferedImage(scaled_xdim, scaled_ydim, original_image.getType());
				scaled_image               = scale_op.filter(original_image, scaled_image);
				
				if(scaled_xdim <= canvas_xdim && scaled_ydim <= canvas_ydim)
				{
				    image          = scaled_image;
				    image_x_offset = 0;
				    image_y_offset = 0;
				    
				    if(scaled_xdim == canvas_xdim)
						canvas_x_offset = 0;
					else
					{
						int delta       = canvas_xdim - scaled_xdim;
						canvas_x_offset = delta / 2;
					}
					
					if(scaled_ydim == canvas_ydim)
						canvas_y_offset = 0;
					else
					{
						int delta       = canvas_ydim - scaled_ydim;
						canvas_y_offset = delta / 2;
					}
				}
				else
				{
					int upper_x     = x - canvas_xdim / 2;
					int upper_y     = y - canvas_ydim / 2;
					
					int delta       = scaled_xdim - canvas_xdim;
					image_x_offset  = delta / 2;
					delta           = scaled_ydim - canvas_ydim;
					image_y_offset  = delta / 2;
					
					image           = scaled_image.getSubimage(upper_x + image_x_offset, upper_y + image_y_offset, canvas_xdim, canvas_ydim);
					canvas_x_offset = 0;
					canvas_y_offset = 0;
				}
			}
        	
        	g.drawImage(image, canvas_x_offset, canvas_y_offset, this);
        }
    }
}