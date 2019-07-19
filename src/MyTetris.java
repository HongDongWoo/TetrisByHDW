import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MyTetris extends JLabel implements Runnable{
	public static final int SQ_COLS = 10;  //10조각의 열
	public static final int SQ_RAWS = 20;  //20조각의 행
	public static final int SQ_SIZE = 25;  // 한 조각의 사이즈
	public static final int NE_SIZE = 15;  //NEXT 한조각 사이즈
	
	public static final int START_X = 4;
	public static final int START_Y = -1;
	
	
	static boolean running = true; //쓰레드 종료 변수
	Random random = new Random();
	static Queue<String> q = new LinkedList<String>(); //배드블럭 변수
	
	String name;
	Point curPoint;
	int curBlock;
	int curStat;
	Color[][] board = new Color[SQ_COLS][SQ_RAWS];
	ArrayList<Integer> nextBlocks = new ArrayList<Integer>();
	
	int totalScore;
	static int speed=1000;
	static int level=1;
	static int remainingTime=3;
	
	JLabel score; 
	JLabel line; 
	JLabel username;
	JLabel Jlevel;
	public MyTetris(String name) {
		
		this.name = name;
		
		score =new JLabel("<html>SCORE<br>"+totalScore+"</html>");
		score.setFont(new Font("serif", Font.BOLD, 20));
		score.setBorder(BorderFactory.createLineBorder(Color.black,3));
		score.setHorizontalAlignment(JLabel.CENTER);
		score.setBounds(5,5,240,60);
		
		Jlevel = new JLabel("<html>LEVEL<br>"+level+"</html>");
		Jlevel.setFont(new Font("serif", Font.BOLD, 20));
		Jlevel.setBorder(BorderFactory.createLineBorder(Color.black,3));
		Jlevel.setHorizontalAlignment(JLabel.CENTER);
		Jlevel.setBounds(5, 68, 117, 77);
		
		username = new JLabel(name);
		username.setFont(new Font("serif", Font.BOLD, 20));
		username.setBounds(5,653,240,42);
		username.setBorder(BorderFactory.createLineBorder(Color.black,3));
		
		
		
		curPoint = new Point(START_X , START_Y);
		curBlock = random.nextInt(7); //무작위로 종류 결정
		curStat = 0;
		for(int i=0; i<SQ_COLS; i++) {
			for(int j=0; j<SQ_RAWS; j++) {
					board[i][j] = Color.BLACK;
			}
		}
		Collections.addAll(nextBlocks, random.nextInt(7),random.nextInt(7),random.nextInt(7));
		
		/* 화면에 컴포넌트 포함 */
		setLayout(null);
		add(score);
		add(Jlevel);
		add(username);
	}
	
	public String getName() {
		return this.name;
	}
	public void init() {
		curPoint = new Point(START_X , START_Y);
		curBlock = random.nextInt(7);
		curStat = 0;
		Collections.shuffle(nextBlocks);
		for(int i=0; i<SQ_COLS; i++) {
			for (int j = 0; j < SQ_RAWS; j++) {
				board[i][j] = Color.BLACK;

			}
		}

		running=true;
	}
	public boolean isCrashed(){
		
		for(Point pt : Block.form[curBlock][curStat]) {
			int X = pt.x + curPoint.x; //편의를 위해 치환
			int Y = pt.y + curPoint.y; //편의를 위해 치환
			
			if( Y < 0) {
				/*안보이는곳에서 충돌한 경우 */
				if(X<0 || X>SQ_COLS-1) {
					return true;
				}
				continue;
			}
			
			if( X < 0 || X> SQ_COLS-1) {
				return true;
			}else if(Y > SQ_RAWS-1) {
				return true;
			}
			else if( board[X][Y] != Color.BLACK ) {
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
		if( count >= 1){
			synchronized (q) {
				q.add(this.name);
				q.notifyAll();
			}
		}
		
		switch(count) {
		case 1:
			totalScore += 100;
			break;
		case 2:
			totalScore += 200;
			break;
		case 3:
			totalScore += 400;
			break;
		case 4:
			totalScore += 800;
			break;
		}
		
		score.setText("<html>SCORE<br>"+totalScore+"</html>");
	}
	
	public void addRow() {
		int hole = random.nextInt(10); /* 배드 블럭 중 구멍의 인덱스 */
		for(int j=1; j< SQ_RAWS; j++) {
			for(int i=0; i< SQ_COLS; i++) {
				board[i][j-1] = board[i][j];
			}
		}
		for(int i=0; i<SQ_COLS; i++) {
			if( i == hole) {
				board[i][SQ_RAWS-1] = Color.BLACK;
			}
			else
				board[i][SQ_RAWS-1] =  Color.white;
		}
		
		if( isCrashed()) {
			curPoint.y--; /*배드 블럭이 순간적으로 올라왔을때 
							블럭 겹침을 방지하고자 현재 포지션 한칸 올려준다. */
			//blockFixed();
		}
		repaint();
	}
	public void deleteRow(int row) {
		for(int j = row-1; j>= 0; j--) {
			for(int i=0; i<SQ_COLS; i++) {
				board[i][j + 1] = board[i][j];
			}
		}
	}
	public void newBlock() {
		curPoint = new Point(START_X , START_Y);
		curStat = 0;
		curBlock = nextBlocks.get(0);
		nextBlocks.remove(0);
		nextBlocks.add(random.nextInt(7));
		
		
		
		if( isCrashed())  {
			curPoint.y--; //블럭겹침 방지용으로 포지션을 변경해준다.
		}
		
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
			int X = pt.x + curPoint.x; //편의를 위해 치환
			int Y = pt.y + curPoint.y; //편의를 위해 치환
			
			if( Y < 0 ) {
				running = false;
				continue;
			}
			
			board[X][Y] = Block.color[curBlock];
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
				if (running) {
					if (board[i][j] == Color.BLACK) {
						g.setColor(Color.black);
						g.fillRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
						g.setColor(Color.white);
						g.drawRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
					} else {
						g.setColor(board[i][j]);
						g.fillRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
						g.setColor(Color.black);
						g.drawRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
					}
				}else {
					/* 죽었을때는 회색으로 블럭들을 회색으로 표시한다 */
					if (board[i][j] == Color.BLACK) {
						g.setColor(Color.black);
						g.fillRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
						g.setColor(Color.white);
						g.drawRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
					} else {
						g.setColor( Color.gray);
						g.fillRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
						g.setColor(Color.black);
						g.drawRect(i * SQ_SIZE, 150+j * SQ_SIZE, SQ_SIZE, SQ_SIZE);
					}
				}
				
			}
		}
		
		/* 블록 그리기 */
		for(Point pt : Block.form[curBlock][curStat]) {
			int X = pt.x + curPoint.x; //편의를 위해 치환
			int Y = pt.y + curPoint.y; //편의를 위해 치환
			
			if( Y < 0) {
				continue;
			}
			g.setColor(Block.color[curBlock]);
			g.fillRect( X*SQ_SIZE,150+Y*SQ_SIZE, SQ_SIZE, SQ_SIZE);
			g.setColor(Color.BLACK);
			g.drawRect(X*SQ_SIZE, 150+Y*SQ_SIZE, SQ_SIZE, SQ_SIZE);
		}
		
		/* NEXT 그리기 */
		Color tmp = Block.color[ nextBlocks.get(0)];
		g.setColor( Color.black );
		g.fillRect(126,68,117,77);
		
		for(Point pt : Block.form[nextBlocks.get(0)][0]) {
			g.setColor(tmp);
			g.fillRect( 160+pt.x*NE_SIZE,90+ pt.y*NE_SIZE, NE_SIZE, NE_SIZE);
			g.setColor(Color.white);
			g.drawRect( 160+pt.x*NE_SIZE,90+ pt.y*NE_SIZE, NE_SIZE, NE_SIZE);
		}
		
		
		
	}

	
	public static void main(String[] args) {
		/* GUI 만드는 부분 */
		JFrame frame = new JFrame("Tetris By HDW");
		
		/* 테트리스 게임 player1 */
		MyTetris player1 = new MyTetris("p1" );
		player1.setPreferredSize(new Dimension(SQ_SIZE*10,700));
		player1.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		/* 테트리스 게임 player 2*/
		MyTetris player2 = new MyTetris("p2");
		player2.setPreferredSize(new Dimension(SQ_SIZE*10,700));
		player2.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		
		/* vs레이블 */
		JLabel vs = new JLabel("<html> :"+remainingTime +"<br>VS</html>");
		vs.setFont(new Font("serif",Font.BOLD,20));
		vs.setHorizontalAlignment(JLabel.CENTER);
		vs.setPreferredSize(new Dimension(100,100));
		
		/* 타이머 선언 */
		Timer t4 = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				remainingTime--;
				if(remainingTime < 0) {
					remainingTime = 3;
					level++;
					if(speed <= 200) {
						if(speed <=100) {
							speed -= 5;
							if(speed<0)
								return;
						}else {
							speed -= 30;
						}
					}else {
						speed -=200;
					}
					/*레벨업시 속도 증가와 함께 한라인 증가 */
					player1.addRow();
					player2.addRow();
					player1.Jlevel.setText("<html>LEVEL<br>"+level+"</html>");
					player2.Jlevel.setText("<html>LEVEL<br>"+level+"</html>");
				}
				vs.setText("<html> :"+remainingTime +"<br>VS</html>");
			}
		};
		
		frame.addKeyListener(new KeyListener() {
			/* 키 리스너 함수 구현 */
			public void keyTyped(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {
				
					switch (e.getKeyCode()) {
					/* 1p용 키 이벤트 */
					case KeyEvent.VK_UP:
						player1.rotateBlock();
						break;
					case KeyEvent.VK_DOWN:
						player1.dropBlock();
						break;
					case KeyEvent.VK_LEFT:
						player1.moveBlock(-1);
						break;
					case KeyEvent.VK_RIGHT:
						player1.moveBlock(+1);
						break;
					case KeyEvent.VK_SPACE:
						player1.dropFast();
						break;
					/* 2p용 키 이벤트 */
					case KeyEvent.VK_I:
						player2.rotateBlock();
						break;
					case KeyEvent.VK_K:
						player2.dropBlock();
						break;
					case KeyEvent.VK_J:
						player2.moveBlock(-1);
						break;
					case KeyEvent.VK_L:
						player2.moveBlock(+1);
						break;
					case KeyEvent.VK_ENTER:
						player2.dropFast();
						break;
					/* q키 누르면 종료 */
					case KeyEvent.VK_Q:
						running = false;
						break;
					}
				
			}
			public void keyReleased(KeyEvent e) {}
		});
		
		
		frame.setLayout(new FlowLayout());
		frame.add(player1);
		frame.add(vs);
		frame.add(player2);
		
		
		
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700,800);
		frame.setVisible(true);
		
		
		Thread t1 = new Thread(player1);
		Thread t2 = new Thread(player2);
		BadBlock bb = new BadBlock(q,player1, player2);
		Thread t3 = new Thread(bb);
		t1.start();
		t2.start();
		t3.start();
		t4.schedule(tt, 1000,1000);
	
		
		
		try {
			t1.join();
			t2.join();
			
		} catch (InterruptedException e1) {
			System.out.println("join실패");
		}
		t3.interrupt();
		t4.cancel();
		
		
		System.out.println("성공적 종료");
		
	}
	@Override
	public void run() {
		while(running) {
			
			try {
				
				dropBlock();
				Thread.sleep(speed);
			} catch (InterruptedException e) {}
			
			
		}
		
		System.out.println(Thread.currentThread().getName() + "종료");
		
	}
	
}
/**
 * 배드블럭 처리 객체(동기화)
 * @author 홍동우(hdw9413@naver.com)
 *
 */
class BadBlock implements Runnable {
	Queue<String> q; //배드블럭을 관리할때 사용하는 큐
	MyTetris p1;
	MyTetris p2;
	
	public BadBlock(Queue<String> q, MyTetris p1, MyTetris p2) {
		this.q = q;
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public void run() {
		/* 동기화 블럭 */
		
		synchronized (q) {
			while (MyTetris.running) {
				try {
					/* Queue 에 데이터가 써질떄까지 기다린다. */
					while(q.isEmpty()) {
						q.wait();
					}
				} catch (InterruptedException e) {
					break;
				}
				int size = q.size();
				for (int i = 0; i < size; i++) {
					if( q.peek().equals( p1.getName())) {
						q.remove();
						p2.addRow();
						
					}else {
						q.remove();
						p1.addRow();
					}
				}
				q.notifyAll();
			}
		}
		System.out.println(Thread.currentThread().getName() + "종료");
	}
	
}



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
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)},
				{new Point(1,0), new Point(1,1), new Point(0,1), new Point(0,2)},
				{new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)},
				{new Point(1,0), new Point(1,1), new Point(0,1), new Point(0,2)}
				
			},
			//막대기
			{
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(1,3)},
				{new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1)},
				{new Point(1,0), new Point(1,1), new Point(1,2), new Point(1,3)}
			},
			//'Z'자 모양
			{
				{new Point(0,1), new Point(1,0), new Point(1,1), new Point(2,0)},
				{new Point(0,0), new Point(0,1), new Point(1,1), new Point(1,2)},
				{new Point(0,1), new Point(1,0), new Point(1,1), new Point(2,0)},
				{new Point(0,0), new Point(0,1), new Point(1,1), new Point(1,2)}
				
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