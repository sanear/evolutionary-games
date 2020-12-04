import javax.swing.JApplet;

import java.awt.Graphics;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimulatorApplet extends JApplet
{
	private static final long serialVersionUID = 1L;
	// Constant variable declarations
	private final int DEFAULT_CANVAS_SIZE = 400;
	private final int DEFAULT_STOP_TIME = -1;
	private final String PAUSE_BUTTON_ISRUNNING_TEXT = "Pause";
	private final String PAUSE_BUTTON_NOTISRUNNING_TEXT = "Resume";
	private final String[] UPDATE_METHOD_NAMES = 
	{
		"Payoff affecting birth and death",
		"Birth-death updating (linear)",
		"Death-birth process",
		"Imitation process",
		"Birth-death of the least fit",
		"Death-birth of the fittest",
		"Imitation of the fittest",
		"Best response dynamics"
	};
	private final DecimalFormat PROPORTION_FMT = new DecimalFormat("0.0000");

	// Instance variable declarations
	private Lattice lattice;
	private boolean isRunning;
	private int drawHead;
	private int time;
	private int size;
	private int stopTime;
	private Thread calculateThread, animateThread;
	private JPanel settingsPanel, infoPanel;
	private CanvasPanel canvas;
	private JLabel ratioLabel, ratioHeadingLabel, currentTimeLabel;
	private JButton startButton, pauseButton, resetButton;
	private JComboBox updateMethodComboBox;
	private JSpinner timeSpinner, dimensionsSpinner, numStrategiesSpinner, interactionRangeSpinner;
	private JSpinner[] initProportionMaskSpinners;
	private JSpinner[][] payoffMatrixSpinners;
	
	/**
	 * Create the applet.
	 */
	public SimulatorApplet()
	{
		isRunning = false;
		time = 0;
		drawHead = 0;
		size = DEFAULT_CANVAS_SIZE;
		stopTime = DEFAULT_STOP_TIME;
	}
	
	public void init()
	{
		lattice = new Lattice();
		setSize(new Dimension(800, 400));
		initializeUI();
		reconcileMode(true);
	}
	
	private void initializeUI()
	{
		setPreferredSize(new Dimension(800, 400));
		setMinimumSize(new Dimension(800, 400));
		getContentPane().setLayout(new GridLayout(1, 2, 0, 0));
		
		JPanel controlPanel = new JPanel();
		getContentPane().add(controlPanel);
		controlPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel buttonPanel = new JPanel();
		controlPanel.add(buttonPanel, BorderLayout.NORTH);
		
		startButton = new JButton("Start");
		startButton.addActionListener(new StartButtonListener());
		buttonPanel.add(startButton);
		
		pauseButton = new JButton(PAUSE_BUTTON_NOTISRUNNING_TEXT);
		pauseButton.addActionListener(new PauseButtonListener());
		pauseButton.setEnabled(false);
		buttonPanel.add(pauseButton);
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ResetButtonListener());
		buttonPanel.add(resetButton);
		
		settingsPanel = new JPanel();
		controlPanel.add(settingsPanel, BorderLayout.CENTER);
		GridBagLayout gbl_settingsPanel = new GridBagLayout();
		gbl_settingsPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_settingsPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_settingsPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_settingsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		settingsPanel.setLayout(gbl_settingsPanel);
		
		JLabel timeLabel = new JLabel("Time");
		GridBagConstraints gbc_timeLabel = new GridBagConstraints();
		gbc_timeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_timeLabel.gridx = 1;
		gbc_timeLabel.gridy = 1;
		settingsPanel.add(timeLabel, gbc_timeLabel);
		
		timeSpinner = new JSpinner();
		timeSpinner.setModel(new SpinnerNumberModel(new Integer(DEFAULT_STOP_TIME), new Integer(-1), null, new Integer(10)));
		((JSpinner.DefaultEditor)timeSpinner.getEditor()).getTextField().setColumns(10);
		GridBagConstraints gbc_timeSpinner = new GridBagConstraints();
		gbc_timeSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_timeSpinner.gridx = 4;
		gbc_timeSpinner.gridy = 1;
		timeSpinner.addChangeListener(new GameTheoryChangeListener());
		settingsPanel.add(timeSpinner, gbc_timeSpinner);
		
		initializeCurrentTimeLabel();
		
		JLabel dimensionsLabel = new JLabel("Dimensions");
		GridBagConstraints gbc_dimensionsLabel = new GridBagConstraints();
		gbc_dimensionsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dimensionsLabel.gridx = 1;
		gbc_dimensionsLabel.gridy = 2;
		settingsPanel.add(dimensionsLabel, gbc_dimensionsLabel);
		
		dimensionsSpinner = new JSpinner();
		dimensionsSpinner.setModel(new SpinnerNumberModel(lattice.getDimensions(), 1, 2, 1));
		GridBagConstraints gbc_dimensionSpinner = new GridBagConstraints();
		gbc_dimensionSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_dimensionSpinner.gridx = 4;
		gbc_dimensionSpinner.gridy = 2;
		dimensionsSpinner.addChangeListener(new GameTheoryChangeListener());
		settingsPanel.add(dimensionsSpinner, gbc_dimensionSpinner);
		
		JLabel numStrategiesLabel = new JLabel("Number of Strategies");
		GridBagConstraints gbc_numStrategiesLabel = new GridBagConstraints();
		gbc_numStrategiesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_numStrategiesLabel.gridx = 1;
		gbc_numStrategiesLabel.gridy = 3;
		settingsPanel.add(numStrategiesLabel, gbc_numStrategiesLabel);
		
		numStrategiesSpinner = new JSpinner();
		numStrategiesSpinner.setModel(new SpinnerNumberModel(lattice.getNumStrategies(), 2, 3, 1));
		GridBagConstraints gbc_numStrategiesSpinner = new GridBagConstraints();
		gbc_numStrategiesSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_numStrategiesSpinner.gridx = 4;
		gbc_numStrategiesSpinner.gridy = 3;
		numStrategiesSpinner.addChangeListener(new GameTheoryChangeListener());
		settingsPanel.add(numStrategiesSpinner, gbc_numStrategiesSpinner);
		
		JLabel interactionRangeLabel = new JLabel("Interaction Range");
		GridBagConstraints gbc_interactionRangeLabel = new GridBagConstraints();
		gbc_interactionRangeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_interactionRangeLabel.gridx = 1;
		gbc_interactionRangeLabel.gridy = 4;
		settingsPanel.add(interactionRangeLabel, gbc_interactionRangeLabel);
		
		interactionRangeSpinner = new JSpinner();
		interactionRangeSpinner.setModel(new SpinnerNumberModel(new Integer(lattice.getInteractionRange()), new Integer(1), null, new Integer(1)));
		GridBagConstraints gbc_interactionRangeSpinner = new GridBagConstraints();
		gbc_interactionRangeSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_interactionRangeSpinner.gridx = 4;
		gbc_interactionRangeSpinner.gridy = 4;
		interactionRangeSpinner.addChangeListener(new GameTheoryChangeListener());
		settingsPanel.add(interactionRangeSpinner, gbc_interactionRangeSpinner);
		
		JLabel lblInitialProportions = new JLabel("Initial Proportions");
		GridBagConstraints gbc_lblInitialProportions = new GridBagConstraints();
		gbc_lblInitialProportions.insets = new Insets(0, 0, 5, 5);
		gbc_lblInitialProportions.gridx = 1;
		gbc_lblInitialProportions.gridy = 5;
		settingsPanel.add(lblInitialProportions, gbc_lblInitialProportions);
		
		initializeInitProportionMaskSpinners();
		
		JLabel lblUpdateMethod = new JLabel("Update Method");
		GridBagConstraints gbc_lblUpdateMethod = new GridBagConstraints();
		gbc_lblUpdateMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblUpdateMethod.gridx = 1;
		gbc_lblUpdateMethod.gridy = 6;
		settingsPanel.add(lblUpdateMethod, gbc_lblUpdateMethod);
		
		updateMethodComboBox = new JComboBox(UPDATE_METHOD_NAMES);
		updateMethodComboBox.setSelectedIndex(lattice.getUpdateMethod());
		GridBagConstraints gbc_updateMethodComboBox = new GridBagConstraints();
		gbc_updateMethodComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_updateMethodComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_updateMethodComboBox.gridx = 4;
		gbc_updateMethodComboBox.gridy = 6;
		updateMethodComboBox.addActionListener(new ComboBoxListener());
		settingsPanel.add(updateMethodComboBox, gbc_updateMethodComboBox);
		
		JLabel payoffMatrixLabel = new JLabel("Payoff Matrix");
		GridBagConstraints gbc_payoffMatrixLabel = new GridBagConstraints();
		gbc_payoffMatrixLabel.insets = new Insets(0, 0, 0, 5);
		gbc_payoffMatrixLabel.gridx = 1;
		gbc_payoffMatrixLabel.gridy = 7;
		settingsPanel.add(payoffMatrixLabel, gbc_payoffMatrixLabel);
		
		initializePayoffMatrixSpinners();
		
		infoPanel = new JPanel();
		controlPanel.add(infoPanel, BorderLayout.SOUTH);
		infoPanel.setLayout(new GridLayout(2, 1, 0, 0));

		initializeRatioLabels();
		
		JPanel displayPanel = new JPanel();
		getContentPane().add(displayPanel);
		displayPanel.setLayout(new BorderLayout(0, 0));
		
		canvas = new CanvasPanel();
		displayPanel.add(canvas, BorderLayout.CENTER);
	}
	
	private void initializeCurrentTimeLabel()
	{
		currentTimeLabel = new JLabel(time + "");
		GridBagConstraints gbc_currentTimeLabel = new GridBagConstraints();
		gbc_currentTimeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_currentTimeLabel.gridx = 3;
		gbc_currentTimeLabel.gridy = 1;
		settingsPanel.add(currentTimeLabel, gbc_currentTimeLabel);
	}
	
	private void updateCurrentTimeLabel()
	{
		currentTimeLabel.setText(time + "");
	}

	private void initializeInitProportionMaskSpinners()
	{
		if (initProportionMaskSpinners != null)
		{
			for (int i=0; i<initProportionMaskSpinners.length; i++)
			{
				settingsPanel.remove(initProportionMaskSpinners[i]);
			}
			settingsPanel.updateUI();
		}
		initProportionMaskSpinners = new JSpinner[lattice.getNumStrategies()];
		for (int i=0; i<initProportionMaskSpinners.length; i++)
		{
			initProportionMaskSpinners[i] = new JSpinner();
			initProportionMaskSpinners[i].setModel(new SpinnerNumberModel(new Double(0), new Double(0), null, new Double(0.2)));
			initProportionMaskSpinners[i].setMinimumSize(new Dimension(74,28));
			initProportionMaskSpinners[i].setPreferredSize(new Dimension(74,28));
			GridBagConstraints gbc_strategy1Spinner = new GridBagConstraints();
			gbc_strategy1Spinner.insets = new Insets(0, 0, 5, 5);
			gbc_strategy1Spinner.gridx = 3+i;		// This is hardcoded!! Not the best, but necessary.
			gbc_strategy1Spinner.gridy = 5;
			initProportionMaskSpinners[i].setValue(lattice.getInitProportionMaskOf(i));
			initProportionMaskSpinners[i].addChangeListener(new GameTheoryChangeListener());
			settingsPanel.add(initProportionMaskSpinners[i], gbc_strategy1Spinner);
		}
	}
	
	private void initializePayoffMatrixSpinners()
	{
		if (payoffMatrixSpinners != null)
		{
			for (int i=0; i<payoffMatrixSpinners.length; i++)
			{
				for (int j=0; j<payoffMatrixSpinners[0].length; j++)
				{
					settingsPanel.remove(payoffMatrixSpinners[i][j]);
				}
			}
			settingsPanel.updateUI();
		}
		payoffMatrixSpinners = new JSpinner[lattice.getNumStrategies()][lattice.getNumStrategies()];
		for (int i=0; i<payoffMatrixSpinners.length; i++)
		{
			for (int j=0; j<payoffMatrixSpinners[0].length; j++)
			{
				payoffMatrixSpinners[i][j] = new JSpinner();
				payoffMatrixSpinners[i][j].setModel(new SpinnerNumberModel(new Double(1), null, null, new Double(0.1)));
				payoffMatrixSpinners[i][j].setMinimumSize(new Dimension(74,28));		// Hardcoded dimensions are lame, but I don't have a choice
				payoffMatrixSpinners[i][j].setPreferredSize(new Dimension(74,28));
				GridBagConstraints gbc_spinner = new GridBagConstraints();
				gbc_spinner.insets = new Insets(0, 0, 0, 5);
				gbc_spinner.gridx = 3+j;												// These are hardcoded!!
				gbc_spinner.gridy = 7+i;
				payoffMatrixSpinners[i][j].setValue((Double) lattice.getPayoffCoefficient(i,j));
				payoffMatrixSpinners[i][j].addChangeListener(new GameTheoryChangeListener());
				settingsPanel.add(payoffMatrixSpinners[i][j], gbc_spinner);
			}
		}
	}
	
	private void initializeRatioLabels()
	{
		// TODO REMOVE EXISTING LABELS
		if (ratioHeadingLabel != null)
		{
			infoPanel.remove(ratioHeadingLabel);
		}
		if (ratioLabel != null)
		{
			infoPanel.remove(ratioLabel);
		}
		String ratioHeading = "";
		if (lattice.numStrategies == 2)
		{
			ratioHeading = "Black : White";
		}
		else if (lattice.numStrategies == 3)
		{
			for(int i=0; i<lattice.getNumStrategies()-1; i++)
			{
				ratioHeading += Lattice.getStrategyColorName(i).toString() + " : ";
			}
			ratioHeading += Lattice.getStrategyColorName(lattice.getNumStrategies()-1).toString();
		}
		ratioHeadingLabel = new JLabel(ratioHeading);
		ratioHeadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(ratioHeadingLabel);
		
		String ratios = "";
		for(int i=0; i<lattice.getNumStrategies()-1; i++)
		{
			ratios += PROPORTION_FMT.format(lattice.getProportionOf(i)) + " : ";
		}
		ratios += PROPORTION_FMT.format(lattice.getProportionOf(lattice.getNumStrategies()-1));
		ratioLabel = new JLabel(ratios);
		ratioLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(ratioLabel);
	}
	
	private void updateRatioLabels()
	{
		String ratios = "";
		for (int i=0; i<lattice.getNumStrategies()-1; i++)
		{
			ratios += PROPORTION_FMT.format(lattice.getProportionOf(i)) + " : ";
		}
		ratios += PROPORTION_FMT.format(lattice.getProportionOf(lattice.getNumStrategies()-1));
		ratioLabel.setText(ratios);
	}
	
	private void applySettings()
	{
		if (!lattice.checkMode())
		{
			System.err.println("The lattice is in an invalid state... EXITING.");
			System.exit(1);
		}

		if (lattice.getDimensions() != (Integer) dimensionsSpinner.getValue())
		{
			lattice.setDimensions((Integer) dimensionsSpinner.getValue());
			reconcileMode(false);
		}
		if (lattice.getNumStrategies() != (Integer) numStrategiesSpinner.getValue())
		{
			lattice.setNumStrategies((Integer) numStrategiesSpinner.getValue());
			reconcileMode(false);
		}
		if (lattice.getInteractionRange() != (Integer) interactionRangeSpinner.getValue())
		{
			lattice.setInteractionRange((Integer) interactionRangeSpinner.getValue());
			reconcileMode(true);
		}
		for (int i=0; i<lattice.getNumStrategies(); i++)
		{
			if (lattice.getInitProportionMaskOf(i) != (Double) initProportionMaskSpinners[i].getValue())
			{
				lattice.setInitProportionMaskOf(i, (Double) initProportionMaskSpinners[i].getValue());
				reconcileMode(true);
			}
		}
		if (lattice.getUpdateMethod() != updateMethodComboBox.getSelectedIndex())
		{
			// NOTE: In update method 1, 2, and 3, negative payoffs are not allowed; hence, we don't allow them to change to
			// negative values.
			if (updateMethodComboBox.getSelectedIndex() == 1 || updateMethodComboBox.getSelectedIndex() == 2 || updateMethodComboBox.getSelectedIndex() == 3)
			{
				for (int i=0; i<lattice.numStrategies; i++)
				{
					for (int j=0; j<lattice.numStrategies; j++)
					{
						if ((Double) payoffMatrixSpinners[i][j].getValue() < 0)
						{
							lattice.setPayoffCoefficient(-1 * (Double) payoffMatrixSpinners[i][j].getValue(), i, j);
							payoffMatrixSpinners[i][j].setValue(-1 * (Double) payoffMatrixSpinners[i][j].getValue());
						}
					}
				}
			}
			lattice.setUpdateMethod(updateMethodComboBox.getSelectedIndex());
			reconcileMode(true);
		}
		for (int i=0; i<lattice.numStrategies; i++)
		{
			for (int j=0; j<lattice.numStrategies; j++)
			{
				if (lattice.getPayoffCoefficient(i, j) != (Double) payoffMatrixSpinners[i][j].getValue())
				{
					if (lattice.getUpdateMethod() == 1 || lattice.getUpdateMethod() == 2 || lattice.getUpdateMethod() == 3)
					{
						// NOTE: In update method 1, negative payoffs are not allowed; hence, we don't allow them to change to
						// negative values.
						if ((Double) payoffMatrixSpinners[i][j].getValue() < 0)
						{
							lattice.setPayoffCoefficient(-1 * (Double) payoffMatrixSpinners[i][j].getValue(), i, j);
							payoffMatrixSpinners[i][j].setValue(-1 * (Double) payoffMatrixSpinners[i][j].getValue());
						}
					}
					else
						lattice.setPayoffCoefficient((Double) payoffMatrixSpinners[i][j].getValue(), i, j);
					reconcileMode(true);
				}
			}
		}
		if (stopTime != (Integer) timeSpinner.getValue())
		{
			stopTime = (Integer) timeSpinner.getValue(); 
		}
	}
	
	private void reconcileMode(boolean canResume)
	{
		// We always make sure that the lattice's mode makes sense before moving on.
		if (!lattice.checkMode())
		{
			System.err.println("The lattice is in an invalid state... EXITING.");
			System.exit(1);
		}
		
		// the (canResume) flag is true if the call to reconcileMode() was made after a change
		// that does not require a new array to be created.
		if (!canResume && !isRunning)
		{
			pauseButton.setEnabled(false);
		}
		
		// IS RUNNING RULES //
		if (isRunning)
		{
			// Disable the start and reset buttons; set the pause button text to "Pause".
			startButton.setEnabled(false);
			pauseButton.setText(PAUSE_BUTTON_ISRUNNING_TEXT);
			pauseButton.setEnabled(true);
			resetButton.setEnabled(false);
			
			// Disable the settings we're not allowed to change while the animation is running.
			dimensionsSpinner.setEnabled(false);
			numStrategiesSpinner.setEnabled(false);
			for (int i=0; i<initProportionMaskSpinners.length; i++)
				initProportionMaskSpinners[i].setEnabled(false);
		}
		// IS NOT RUNNING RULES //
		else
		{
			startButton.setEnabled(true);
			pauseButton.setText(PAUSE_BUTTON_NOTISRUNNING_TEXT);
			resetButton.setEnabled(true);
			
			dimensionsSpinner.setEnabled(true);
			numStrategiesSpinner.setEnabled(true);
			for(int i=0; i<initProportionMaskSpinners.length; i++)
				initProportionMaskSpinners[i].setEnabled(true);
		}
		
		// ONE DIMENSION RULES //
		if (lattice.getDimensions() == 1)
		{
			interactionRangeSpinner.setEnabled(true);
		}
		// TWO DIMENSION RULES //
		else if (lattice.getDimensions() == 2)
		{
			interactionRangeSpinner.setEnabled(false);
		}
		
		if (payoffMatrixSpinners.length != lattice.getNumStrategies() || 
				initProportionMaskSpinners.length != lattice.getNumStrategies())
		{
			initializePayoffMatrixSpinners();
			initializeRatioLabels();
			initializeInitProportionMaskSpinners();
		}
	}
	
	private class Animator implements Runnable
	{
		@Override
		public void run()
		{
			Thread me = Thread.currentThread();
			for (int i=0; me == animateThread && isRunning; i++)
			{
				canvas.repaint();
				if (lattice.getDimensions() == 1)
					drawHead++;
				updateCurrentTimeLabel();
				updateRatioLabels();
				reconcileMode(true);
				try
				{
					Thread.sleep(17);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			isRunning = false;
			animateThread = null;
		}
	}
	
	private class Calculator implements Runnable
	{
		@Override
		public void run()
		{
			for (int i = 0; isRunning; i++)
			{
				double maxRate = lattice.getMaxRate();
				double size = lattice.getSize();
				time = (int) ((double) i / (double) (maxRate*size*size));
				if (time >= stopTime && stopTime >= 0)
				{
					stop();
				}
				for (int j=0; j<lattice.numStrategies; j++)
				{
					if (lattice.getProportionOf(j) == 1.0)
					{
						stop();
					}
				}
				lattice.chooseAndUpdateNode();
			}
			isRunning = false;
			calculateThread = null;
		}
		private void stop()
		{
			updateCurrentTimeLabel();
			isRunning = false;
			reconcileMode(true);
		}
	}
	
	private class CanvasPanel extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (lattice.dimensions == 1)
			{
				lattice.draw1D(g, drawHead, size);
			}
			else if (lattice.dimensions == 2)
			{
				lattice.draw2D(g, size);
			}
		}
	}
	
	private class StartButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (!isRunning)
			{
				isRunning = true;
				time = 0;
				double[] initProportionMasksCopy = new double[lattice.getNumStrategies()];
				for(int i=0; i<lattice.getNumStrategies(); i++)
				{
					initProportionMasksCopy[i] = (Double) initProportionMaskSpinners[i].getValue();
				}
				double[][] payoffMatrixCopy = new double[lattice.getNumStrategies()][lattice.getNumStrategies()];
				for(int i=0; i<lattice.getNumStrategies(); i++)
				{
					for(int j=0; j<lattice.getNumStrategies(); j++)
					{
						payoffMatrixCopy[i][j] = (Double) payoffMatrixSpinners[i][j].getValue();
					}
				}
				lattice = new Lattice((int) (Integer) dimensionsSpinner.getValue(), 
									  (int) (Integer) numStrategiesSpinner.getValue(), 
									  (int) (Integer) interactionRangeSpinner.getValue(), 
									  initProportionMasksCopy,
									  updateMethodComboBox.getSelectedIndex(),
									  payoffMatrixCopy);
				reconcileMode(true);
				animateThread = new Thread(new Animator());
				calculateThread = new Thread(new Calculator());
				animateThread.start();
				calculateThread.start();
			}
		}
	}
	
	private class PauseButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (isRunning)
			{
				isRunning = false;
				reconcileMode(true);
			}
			else
			{
				isRunning = true;
				animateThread = new Thread(new Animator());
				calculateThread = new Thread(new Calculator());
				animateThread.start();
				calculateThread.start();
			}
		}
	}
	
	private class ResetButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			getContentPane().removeAll();
			lattice = new Lattice();
			isRunning = false;
			drawHead = 0;
			size = DEFAULT_CANVAS_SIZE;
			stopTime = DEFAULT_STOP_TIME;
			initializeUI();
			reconcileMode(false);
		}
	}
	
	private class GameTheoryChangeListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			applySettings();
		}
	}
	
	private class ComboBoxListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			applySettings();
		}
	}
}
