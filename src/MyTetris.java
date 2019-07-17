import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MyTetris extends JLabel implements Runnable{
	public static final int SQ_COLS = 10;
	public static final int SQ_RAWS = 20;
	public static final int SQ_SIZE = 30;
	
	
	Point curPoint;
	int curBlock;
	int[][] board = new int[SQ_COLS][SQ_RAWS];
	ArrayList<Integer> nextBlocks = new ArrayList<Integer>();
	
	public MyTetris() {
		curPoint = new Point(5,0);
		curBlock = 0;
		Collections.addAll(nextBlocks, 1,2,3,4,5,6,7);
		Collections.shuffle(nextBlocks);
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Tetris");
		
		MyTetris panel = new MyTetris();
		panel.setPreferredSize(new Dimension(300,600));
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		frame.setLayout(new FlowLayout());
		frame.add(panel);
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_UP:
					System.out.println("업");
					break;
				case KeyEvent.VK_DOWN:
					System.out.println("다운");
					break;
				case KeyEvent.VK_LEFT:
					System.out.println("왼");
					break;
				case KeyEvent.VK_RIGHT:
					System.out.println("오");
					break;
				case KeyEvent.VK_SPACE:
					System.out.println("스페이스");
					break;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		
		Thread t1 = new Thread(panel);
		t1.start();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(320,650);
		frame.setVisible(true);
	}
	@Override
	public void run() {
		System.out.println("쓰레드 실행됨");
		System.out.println("쓰레드 종료");
	}
	
	
}

class Block {
	private Point[][][] shapes = {
			//네모
			{
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)}
			},
			//T자 모양
			{
				{new Point(0,1), new Point(1,0), new Point(1,1), new Point(1,2)},
				{new Point(1,0), new Point(0,1), new Point(1,1), new Point(2,1)},
				{new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,1)},
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,0)}
			},
			//'ㄱ'자 모양
			{
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(0,2)},
				{new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)},
				{new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,0)},
				{new Point(0,0), new Point(1,0), new Point(2,0), new Point(2,1)}
			},
			//'S'자 모양
			{
				{new Point(1,0), new Point(1,1), new Point(0,1), new Point(0,2)},
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)},
				{new Point(1,0), new Point(1,1), new Point(0,1), new Point(0,2)},
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)}
			},
			//막대기
			{
				{new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1)},
				{new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1)}
			},
			//'Z'자 모양
			{
				{new Point(0,0), new Point(0,1), new Point(1,1), new Point(1,2)},
				{new Point(0,1), new Point(1,0), new Point(1,1), new Point(2,0)},
				{new Point(0,0), new Point(0,1), new Point(1,1), new Point(1,2)},
				{new Point(0,1), new Point(1,0), new Point(1,1), new Point(2,0)}
			},
			//'ㄴ'자 모양
			{
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(1,2)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(2,0)},
				{new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,2)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(2,0)}
			}
	};
	
	
	public int type;  //무슨 종류의 블럭인지 저장
	public int status=0;//4가지의 회전상태중 하나를 저장
	
	
	public Block(int type) {
		this.type = type;
		
	}
	
	
}
