import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class CompilerGUI extends JFrame {
	JButton loadButton;
	JButton compileButton;
	JButton runButton;

	JTextArea textArea;
	JScrollPane scrollPane;

	public CompilerGUI() {
		loadButton = new JButton("...");
		compileButton = new JButton("Compile");
		runButton = new JButton("Run");

		textArea = new JTextArea();
		textArea.setTabSize(4);
		scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						File file = fileChooser.getSelectedFile();
						Scanner scanner = new Scanner(file);
						textArea.setText("");
						while (scanner.hasNext()) {
							textArea.append(scanner.nextLine() + "\n");
						}
					} catch (FileNotFoundException error) {}
				}
			}
		});

		compileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				compile();
			}
		});

		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				compile();
				Simpletron simpletron = new Simpletron("temp.txt");
				simpletron.loadProgram();
				simpletron.executeProgram();
			}
		});

		JPanel panel = new JPanel(new FlowLayout());
		panel.add(loadButton);
		panel.add(compileButton);
		panel.add(runButton);

		add(panel, BorderLayout.SOUTH);

	}

	public void compile() {
		try {
			File file = new File("temp.smp");
			PrintWriter writer = new PrintWriter(file);
			writer.print(textArea.getText());
			writer.close();
			
			Compiler compiler = new Compiler();
			compiler.compileProgram("temp.smp");
		} catch (FileNotFoundException error) {System.out.println("error");}
	}

	public static void main(String [] args) {
		CompilerGUI frame = new CompilerGUI();
		frame.setTitle("Compiler");
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}