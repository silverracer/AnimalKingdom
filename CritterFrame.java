import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;

public class CritterFrame extends JFrame {
    private static final long serialVersionUID = 1L; // Added for serializable classes
    private CritterModel myModel;
    private CritterPanel myPicture;
    private Timer myTimer;
    private JButton[] countButtons;
    private JButton stepCountButton;
    private boolean started;
    private static boolean created;

    public CritterFrame(int width, int height) {
        if (created) {
            throw new RuntimeException("Only one world allowed");
        }
        created = true;

        setupFrame(width, height);
        initializeTimer();
        createControlPanel();
    }

    private void setupFrame(int width, int height) {
        setTitle("Critter Simulation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        myModel = new CritterModel(width, height);
        myPicture = new CritterPanel(myModel);
        add(myPicture, BorderLayout.CENTER);
    }

    private void initializeTimer() {
        ActionListener updateAction = e -> performStep();
        myTimer = new Timer(0, updateAction);
        myTimer.setCoalesce(true);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.add(createSpeedSlider());
        controlPanel.add(createControlButton("Start", e -> myTimer.start()));
        controlPanel.add(createControlButton("Stop", e -> myTimer.stop()));
        controlPanel.add(createControlButton("Step", e -> performStep()));
        controlPanel.add(createControlButton("Debug", e -> toggleDebug()));
        controlPanel.add(createControlButton("Next 100", e -> performMultipleSteps(100)));
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JSlider createSpeedSlider() {
        JSlider speedSlider = new JSlider();
        speedSlider.setValue(20);
        speedSlider.addChangeListener(e -> {
            double delay = 1000.0 / (1 + Math.pow(speedSlider.getValue(), 0.3)) - 180;
            myTimer.setDelay((int) delay);
        });
        speedSlider.setToolTipText("Adjust simulation speed");
        return speedSlider;
    }

    private JButton createControlButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

    public void start() {
        if (started) {
            return;
        }
        if (myModel.getCounts().isEmpty()) {
            System.out.println("Nothing to simulate—no critters");
            return;
        }
        started = true;
        setupCountDisplay();
        myModel.updateColorString();
        pack();
        setVisible(true);
    }

    private void setupCountDisplay() {
        JPanel countPanel = new JPanel(new GridLayout(myModel.getCounts().size() + 1, 1));
        countButtons = new JButton[myModel.getCounts().size()];

        for (int i = 0; i < countButtons.length; i++) {
            countButtons[i] = new JButton();
            countPanel.add(countButtons[i]);
        }

        stepCountButton = new JButton();
        stepCountButton.setForeground(Color.BLUE);
        countPanel.add(stepCountButton);

        add(countPanel, BorderLayout.EAST);
        updateCountDisplay();
    }

    private void updateCountDisplay() {
        Set<Map.Entry<String, Integer>> counts = myModel.getCounts();
        int maxCount = 0;
        int maxIndex = 0;

        int i = 0;
        for (Map.Entry<String, Integer> entry : counts) {
            String displayText = String.format("%s = %4d", entry.getKey(), entry.getValue());
            countButtons[i].setText(displayText);
            countButtons[i].setForeground(Color.BLACK);

            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxIndex = i;
            }
            i++;
        }

        if (countButtons.length > 0) {
            countButtons[maxIndex].setForeground(Color.RED);
        }
        stepCountButton.setText(String.format("Step = %5d", myModel.getSimulationCount()));
    }

    public void add(int number, Class<? extends Critter> critterClass) {
        if (started) {
            return;
        }
        myModel.add(number, critterClass);
    }

    private void performStep() {
        myModel.update();
        updateCountDisplay();
        myPicture.repaint();
    }

    private void performMultipleSteps(int steps) {
        myTimer.stop();
        while (myModel.getSimulationCount() % steps != 0) {
            myModel.update();
        }
        updateCountDisplay();
        myPicture.repaint();
    }

    private void toggleDebug() {
        myModel.toggleDebug();
        myPicture.repaint();
    }
}
