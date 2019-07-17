import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MyTetris extends JLabel implements Runnable, KeyListener{
	public static final int SQ_COLS = 10;  //10조각의 열
	public static final int SQ_RAWS = 20;  //20조각의 행
	public static final int SQ_SIZE = 30;   // 한 조각의 사이즈
	
	
	boolean running = true; //쓰레드 종료 변수
	Random random = new Random();
	
	boolean fixed = false;
	Point curPoint;
	int curBlock;
	int curStat;
	Color[][] board = new Color[SQ_COLS][SQ_RAWS];
	ArrayList<Integer> nextBlocks = new ArrayList<Integer>();
	
	public MyTetris() {
		addKeyListener(this);
		this.requestFocus();
		setFocusable(true);
		
		curPoint = new Point(4,0);
		curBlock = random.nextInt(7); //무작의로 종류 결정
		curStat = 0;
		for(int i=0; i<SQ_COLS; i++) {
			for(int j=0; j<SQ_RAWS; j++) {
				board[i][j] = Color.BLACK;
			}
		}
		Collections.addAll(nextBlocks, random.nextInt(7),random.nextInt(7),random.nextInt(7));
	}
	
	public void init() {
		curPoint = new Point(5,0);
		curBlock = random.nextInt(7);
		Collections.shuffle(nextBlocks);
		for(int i=0; i<SQ_COLS; i++) {
			for(int j=0; j<SQ_RAWS; j++) {
				board[i][j] = Color.BLACK;
			}
		}
	}
	public boolean isCrashed(){
		
		for(Point pt : Block.form[curBlock][curStat]) {
			int X = pt.x + curPoint.x; //편의를 위해 치환
			int Y = pt.y + curPoint.y; //편의를 위해 치환
			if( X < 0 || X> SQ_COLS-1) {
				return true;
			}else if(Y > SQ_RAWS-1) {
				fixed =true;
				return true;
			}
			else if( board[X][Y] != Color.BLACK ) {
				fixed = true;
				return true;
			}
			
		}
		
		return false;
	}
	public void removeRows() {
		int sum=0;
		int count=0;
		for(int j = SQ_RAWS -1; j >= 0; j--) {
			for(int i=0; i<SQ_COLS;i++) {
				if(board[i][j] != Color.BLACK) {
					sum++;
				}
			}
			if( sum == 10) {
				deleteRow(j);
				j++;
				count++;
			}
			sum=0;
		}
		if( count >= 3)
			System.out.println("3줄 이상 삭제");
	}
	public void deleteRow(int row) {
		for(int j = row-1; j>= 0; j--) {
			for(int i=0; i<SQ_COLS; i++) {
				board[i][j + 1] = board[i][j];
			}
		}
	}
	public void newBlock() {
		curPoint = new Point(4,0);
		curStat = 0;
		curBlock = nextBlocks.get(0);
		nextBlocks.remove(0);
		nextBlocks.add(random.nextInt(7));
	}
	public void dropBlock() {
		curPoint.y++;
		if( isCrashed() ) {
			curPoint.y--;
			blockFixed();
		} 
		
		
		repaint();
	}
	public void dropFast() {
		do{curPoint.y++;}
		while( !isCrashed() );
		curPoint.y--;
		blockFixed();
		repaint();
	}
	public void moveBlock(int value) {
		curPoint.x += value;
		if( isCrashed() ) {
			curPoint.x -= value;
		}
		repaint();
	}
	public void rotateBlock() {
		int tmp = curStat++;
		if( curStat >3) {
			curStat =0;
		}
		if(isCrashed()) {
			curStat = tmp;
		}
		repaint();
	}
	
	public void blockFixed() {
		for(Point pt : Block.form[curBlock][curStat]) {
			board[pt.x + curPoint.x][pt.y + curPoint.y] = Block.color[curBlock];
		}
		
		removeRows();
		newBlock();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		/* 배경 그리기 */
		for(int i=0; i<SQ_COLS; i++) {
			for(int j=0; j<SQ_RAWS; j++) {
				/* 블럭이 없는 곳은 검정색으로 색칠 */
				if( board[i][j] == Color.BLACK) {
					g.setColor(Color.black);
					g.fillRect(i*SQ_SIZE, j*SQ_SIZE, SQ_SIZE, SQ_SIZE);
					g.setColor(Color.white);
					g.drawRect(i*SQ_SIZE, j*SQ_SIZE, SQ_SIZE, SQ_SIZE);
				}else {
					g.setColor(board[i][j]);
					g.fillRect(i*SQ_SIZE, j*SQ_SIZE, SQ_SIZE, SQ_SIZE);
					g.setColor(Color.black);
					g.drawRect(i*SQ_SIZE, j*SQ_SIZE, SQ_SIZE, SQ_SIZE);
				}
				
			}
		}
		
		/* 블록 그리기 */
		for(Point pt : Block.form[curBlock][curStat]) {
			g.setColor(Block.color[curBlock]);
			g.fillRect( (curPoint.x + pt.x)*SQ_SIZE, (curPoint.y+pt.y)*SQ_SIZE, SQ_SIZE, SQ_SIZE);
			g.setColor(Color.BLACK);
			g.drawRect((curPoint.x + pt.x)*SQ_SIZE, (curPoint.y+pt.y)*SQ_SIZE, SQ_SIZE, SQ_SIZE);
		}
		
	}

	public static void main(String[] args) {
		/* GUI 만드는 부분 */
		JFrame frame = new JFrame("Tetris By HDW");
		
		MyTetris panel = new MyTetris();
		panel.setPreferredSize(new Dimension(300,600));
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		frame.setLayout(new FlowLayout());
		frame.add(panel);
		
		Thread t1 = new Thread(panel);
		t1.start();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(320,650);
		frame.setVisible(true);
	}
	@Override
	public void run() {
		System.out.println("쓰레드 실행됨");
		while(running) {
			
			try {
				Thread.sleep(1000);
				dropBlock();
			} catch (InterruptedException e) {}
		}
		
		System.out.println("쓰레드 종료");
		
	}
	/* 키 리스너 함수 구현 */
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP:
			rotateBlock();
			break;
		case KeyEvent.VK_DOWN:
			dropBlock();
			break;
		case KeyEvent.VK_LEFT:
			moveBlock(-1);
			break;
		case KeyEvent.VK_RIGHT:
			moveBlock(+1);
			break;
		case KeyEvent.VK_SPACE:
			dropFast();
			break;
		case KeyEvent.VK_Q:
			running = false;
			break;
		}
	}
	public void keyReleased(KeyEvent e) {}
}
/**
 * 블럭의 형태를 좌표형식으로 표현하기 위한 일종의 구조체
 * @author 홍동우(hdw9413@naver.com)
 *
 */
class Block{
	public static final Color[] color = {Color.yellow, new Color(0xFF00FF), Color.orange, Color.green,
			new Color(0x00FFFF), Color.RED, Color.blue};
	
	public static final Point[][][] form = {
			//네모
			{
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)},
				{new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)}
			},
			//T자 모양
			{
				{new Point(1,0), new Point(0,1), new Point(1,1), new Point(2,1)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(0,1)},
				{new Point(0,1), new Point(1,1), new Point(1,2), new Point(2,1)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(2,1)}
			},
			//'ㄱ'자 모양
			{
				{new Point(2,0), new Point(0,1), new Point(1,1), new Point(2,1)},
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(1,2)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(0,2)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(2,2)}
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
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(1,3)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(1,3)},
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
				{new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(0,2)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(2,2)},
				{new Point(1,0), new Point(2,0), new Point(1,1), new Point(1,2)}
			}
	};
}