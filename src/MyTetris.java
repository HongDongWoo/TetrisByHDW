import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class MyTetris extends JLabel implements Runnable{
	public static final int SQ_COLS = 10;  //10조각의 열
	public static final int SQ_RAWS = 20;  //20조각의 행
	public static final int SQ_SIZE = 25;  //한 조각의 사이즈
	public static final int NE_SIZE = 15;  //NEXT 한조각 사이즈
	
	public static final int START_X = 4;  //블럭 첫 기준 위치 X
	public static final int START_Y = -1; //블럭 첫 기준 위치 Y
	
	static int gameStat=0; //0은 새게임 1은 다시하기 2는 종료 3는 아무동작 안함
	static boolean running = true; //쓰레드 종료 변수
	Random random = new Random(); //랜덤 변수
	static Queue<String> q = new LinkedList<String>(); //배드블럭 변수
	
	/*유저 정보 */
	private String name; //이름을 받기위한 변수
	User info; //유저 정보 담는 클래스
	boolean isWinner=true;  //게임에서 이기면 true 지면 false가 됨
	
	
	Point curPoint; //현재 블록 기준 위치좌표
	int curBlock;  //현재 블록의 종류
	int curStat;   //현재 블록의 회전상태
	Color[][] board = new Color[SQ_COLS][SQ_RAWS]; //게임보드판
	ArrayList<Integer> nextBlocks = new ArrayList<Integer>(); //다음 블록들을 저장하는 리스트
	
	private int totalScore;  //총 점수
	public static int speed=1000; //게임 스피드. 숫자가 적을수록 빨라짐
	public static int level=1; //게임 레벨
	public static int remainingTime=20; //다음 레벨까지 남은 시간
	
	/* 레이블 표현하기 위한 변수 */
	JLabel score; 
	JLabel line; 
	JLabel username;
	JLabel Jlevel;
	
	
	/* 패널 초기화 */
	public MyTetris() {
		
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
		username.setFont(new Font("serif", Font.BOLD, 14));
		username.setBounds(5,653,240,42);
		username.setBorder(BorderFactory.createLineBorder(Color.black,3));
		
		
		
		curPoint = new Point(START_X , START_Y);
		curBlock = random.nextInt(7); //무작위로 종류 결정
		curStat = 0;
		for(int i=0; i<SQ_COLS; i++) {
			for(int j=0; j<SQ_RAWS; j++) {
					board[i][j] = Color.black;
			}
		}
		Collections.addAll(nextBlocks, random.nextInt(7),random.nextInt(7),random.nextInt(7));
		
		/* 화면에 컴포넌트 포함 */
		setLayout(null);
		add(score);
		add(Jlevel);
		add(username);
	}
	public void init() {
		totalScore=0;  //총 점수
		speed=1000; //게임 스피드. 숫자가 적을수록 빨라짐
		level=1; //게임 레벨
		remainingTime=20; //다음 레벨까지 남은 시간
		isWinner=true;
		
		score.setText("<html>SCORE<br>"+totalScore+"</html>"); //스코어 초기화
		Jlevel.setText("<html>LEVEL<br>"+level+"</html>"); //레벨 초기화
		
		
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
	
	@Override /* 패널의 메인 쓰레드 부분 */
	public void run() {
		while(running) {
			try {
				dropBlock();
				Thread.sleep(speed);
			} catch (InterruptedException e) {}
		}
		System.out.println(Thread.currentThread().getName() + "종료");
	}
	
	public String getName() {
		return this.name;
	}
	public boolean checkName(String name) {
		
		if( name ==null) {
			return false;
		}
		
		String tmp = name.strip();
		if( tmp.equals("")) {
			return false;
		}else{
			this.name = tmp;
			//username.setText(this.name);
			return true;
		}
	}
	/* 다시하기 또는 새게임 할때 상태 초기화 하는 함수 */
	
	/* 블럭이 부딪혔는지 검사 */
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
			
			/* 양쪽 옆 벽 검사 */
			if( X < 0 || X> SQ_COLS-1) {
				return true;
				
			}else if(Y > SQ_RAWS-1) { //바닥 벽 검사
				return true;
			}
			else if( board[X][Y] != Color.BLACK ) {//기존블럭과 충돌 검사
				return true;
			}
			
		}
		
		return false;
	}
	/* 줄들을 삭제하는 함수 */
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
		/* 3줄 이상 삭제 했을때 */
		if( count >= 3){
			synchronized (q) {
				/* 큐에 이름 넣고 컨트롤러 깨움 */
				q.add(this.name);
				q.notifyAll();
			}
		}
		
		/* 지운 줄 숫자별 점수 차등 획득 */
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
	/* 배드블럭 생성 함수 */
	public void addRow() {
		int hole = random.nextInt(10); /* 배드 블럭 중 구멍의 인덱스 */
		for(int j=1; j< SQ_RAWS; j++) {
			for(int i=0; i< SQ_COLS; i++) {
				board[i][j-1] = board[i][j];
			}
		}
		/* 배드블럭은 흰색 구멍은 검은색 */
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
	/* 블럭 처음에 생성하는 함수 */
	public void newBlock() {
		curPoint = new Point(START_X , START_Y);
		curStat = 0;
		curBlock = nextBlocks.get(0);
		nextBlocks.remove(0);
		nextBlocks.add(random.nextInt(7)); //7가지 블럭중 랜덤으로
		
		if( isCrashed())  {
			curPoint.y--; //블럭겹침 방지용으로 포지션을 변경해준다.
		}
		
	}
	
	/* 매초 마다(레벨 오를수록 빨리) 한칸씩 블럭 떨어뜨림 */
	public void dropBlock() {
		curPoint.y++;
		if( isCrashed() ) {
			curPoint.y--;
			blockFixed(); 
		} 
		
		
		repaint();
	}
	/* 블럭 빠르게 떨어뜨려 고정시키는 함수 */
	public void dropFast() {
		do{curPoint.y++;}
		while( !isCrashed() );
		curPoint.y--;
		blockFixed();
		repaint();
	}
	/*블럭 왼쪽 오른쪽으로 이동시키는 함수 */
	public void moveBlock(int value) {
		curPoint.x += value;
		if( isCrashed() ) {
			curPoint.x -= value;
		}
		repaint();
	}
	/* 블럭 회전 함수 */
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
	/* 블럭 고정시키는 함수 
	 * (보드판에 색깔 입력)*/
	public void blockFixed() {
		for(Point pt : Block.form[curBlock][curStat]) {
			int X = pt.x + curPoint.x; //편의를 위해 치환
			int Y = pt.y + curPoint.y; //편의를 위해 치환
			
			if( Y < 0 ) {
				running = false; //모든 쓰레드를 종료
				isWinner = false; //진 사람(해당 패널의)은 false로 만듬
				continue;
			}
			
			board[X][Y] = Block.color[curBlock];
		}
		
		removeRows();
		newBlock();
	}
	
	@Override /* JPanel 그리기 */
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
		
		/* NEXT(다음 블록) 그리기 */
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

	
	/* main 함수 */
	public static void main(String[] args) {
		/* GUI 만드는 부분 */
		JFrame frame = new JFrame("Tetris By HDW"); //프레임 생성
		
		/* 테트리스 게임 player1 패널 */
		MyTetris player1 = new MyTetris();
		player1.setPreferredSize(new Dimension(SQ_SIZE*10,700));
		player1.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		/* 테트리스 게임 player2 패널*/
		MyTetris player2 = new MyTetris();
		player2.setPreferredSize(new Dimension(SQ_SIZE*10,700));
		player2.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		/* vs레이블과 남은 시간 표현*/
		JLabel vs = new JLabel("<html> :"+remainingTime +"<br><br><br>VS</html>");
		vs.setFont(new Font("serif",Font.BOLD,20));
		vs.setHorizontalAlignment(JLabel.CENTER);
		
		/* 중앙 패널 */
		JPanel middle = new JPanel();
		middle.setPreferredSize(new Dimension(100,300));
		middle.add(vs);
		
		

		
		/* 키 리스너 함수 구현 */
		frame.addKeyListener(new KeyListener() {
			
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
					/* q키 누르면 종료 
					case KeyEvent.VK_Q:
						running = false;
						break;*/
					}
				
			}
			public void keyReleased(KeyEvent e) {}
		});
		
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700,800);
		frame.setVisible(true);
		
		frame.setLayout(new FlowLayout());
		frame.add(player1);
		frame.add(middle);
		frame.add(player2);
		
		/* 여기서 부터 메뉴( 새게임, 재대결, 종료하기) 루프 */
		do {
	
			if (gameStat == 0) { // 새게임
				/* 1P이름을 입력받고, 적절하지 않으면 루프를 돈다 */
				do {
					String p1 = (String) JOptionPane.showInputDialog(frame, "1P 이름을 입력하세요");
					if (player1.checkName(p1)) {
						/* 공백이 아닐경우 루프를 빠져나온다 */
						break;
					} else {
						JOptionPane.showMessageDialog(frame, "적절하지 않은 이름입니다.(공백X)", "적절하지 않은 이름",
								JOptionPane.WARNING_MESSAGE);
					}
				} while (true);
				/* 2P 이름을 입력받고, 적절하지 않을경우 루프돈다 */
				do {
					String p2 = (String) JOptionPane.showInputDialog(frame, "2P 이름을 입력하세요");
					if (player2.checkName(p2)) {
						if (player1.getName().equals(player2.getName())) {
							JOptionPane.showMessageDialog(frame, "1P와 이름이 똑같습니다. 다시 입력하세요", "이름이 똑같음",
									JOptionPane.WARNING_MESSAGE);
						} else {
							/* 공백이 아니거나 1P와 동일이름이 아닐경우 루프를 빠져나온다 */
							break;
						}

					} else {
						JOptionPane.showMessageDialog(frame, "적절하지 않은 이름입니다.(공백X)", "적절하지 않은 이름",
								JOptionPane.WARNING_MESSAGE);
					}

				} while (true);
					
				
				
				player1.init(); //1P패널 초기화
				player2.init(); //2P패널 초기화
				
			} else if (gameStat == 1) { // 다시하기
				player1.init(); //1P패널 초기화
				player2.init(); //2P패널 초기화
			} else { //종료하기
				break;
			}

			/* 쓰레드 생성 */
			Thread t1 = new Thread(player1); // p1패널
			Thread t2 = new Thread(player2); // p2패널
			BadBlock controller = new BadBlock(q, player1, player2); // 컨트롤러
			Thread t3 = new Thread(controller);
			Timer t4 = new Timer(); // 타이머 객체
			TimerTask tt = new TimerTask() { // 1초 지날때 마다 remainingTime 변수를 1씩 낮춘다.
				public void run() {
					remainingTime--;
					if (remainingTime < 0) {
						remainingTime = 20;
						level++;
						if (speed <= 200) {
							if (speed <= 100) {
								speed -= 5;
								if (speed < 0)
									return;
							} else {
								speed -= 30;
							}
						} else {
							speed -= 200;
						}
						/* 레벨업시 속도 증가와 함께 한라인 증가 */
						player1.addRow();
						player2.addRow();
						player1.Jlevel.setText("<html>LEVEL<br>" + level + "</html>");
						player2.Jlevel.setText("<html>LEVEL<br>" + level + "</html>");
					}
					vs.setText("<html> :" + remainingTime + "<br>VS</html>");
				}
			};
			/* 게임실행중 유저정보 보여주기 */
			player1.username.setText("["+player1.getName()+"]"+" 승:"+controller.map.get(player1.getName()).winNum
					+" ,패:"+ controller.map.get(player1.getName()).loseNum
					+" ,승률:"+String.format("%.1f", controller.map.get(player1.getName()).winRate)+"%");
			player2.username.setText("["+player2.getName()+"]"+" 승:"+controller.map.get(player2.getName()).winNum
					+" ,패:"+controller.map.get(player2.getName()).loseNum
					+" ,승률:"+String.format("%.1f", controller.map.get(player2.getName()).winRate)+"%");
			
			/* 쓰레드 스타트 */
			t1.start();
			t2.start();
			t3.start();
			t4.schedule(tt, 1000, 1000);

			try {
				t1.join();
				t2.join();
			} catch (InterruptedException e1) {
				System.out.println("join실패");
			}
			/* 패널이 끝나면 중앙 컨트롤러와 타이머 정지 */
			t3.interrupt();
			t4.cancel();

			String winner = controller.p1.isWinner ? controller.p1.getName() : controller.p2.getName();
			String loser = controller.p1.isWinner ? controller.p2.getName() : controller.p1.getName();
			
			/* 컨트롤러가 조인한 후에 경기결과를 보여준다 */
			try {
				t3.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			new MyDialog(frame, winner, loser); // 경기 결과 대화창
			new MyDialog2(frame, controller.map ); // 테이블 결과창
		} while (gameStat != 2); // 종료하기 버튼을 누를때까지 루프를 돈다.

		// frame.setVisible(false);
		if (gameStat == 2) {
			frame.dispose();
		}
		System.out.println("성공적 종료");
	}
}
/**
 *경기결과를 보여주는 대화상자
 * @author 홍동우(hdw9413@naver.com)
 *
 */
class MyDialog extends JDialog {
	private JPanel panel;
	private JLabel label = new JLabel("승");
	private JLabel label2 = new JLabel("패");
	private JLabel winner = new JLabel("");
	private JLabel loser = new JLabel("");
	private JButton okBtn = new JButton("확인");

	public MyDialog(JFrame frame, String winner, String loser) {
		super(frame);
		this.winner.setText(winner);
		this.loser.setText(loser);

		/* 여기부터 컴포넌트 설정 */
		panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 3));

		label.setFont(new Font("serif", Font.BOLD, 100));
		label.setHorizontalAlignment(JLabel.CENTER);

		label2.setFont(new Font("serif", Font.BOLD, 100));
		label2.setHorizontalAlignment(JLabel.CENTER);

		this.winner.setFont(new Font("serif", Font.BOLD, 30));
		this.winner.setHorizontalAlignment(JLabel.CENTER);
		this.winner.setPreferredSize(new Dimension(130, 50));

		this.loser.setFont(new Font("serif", Font.BOLD, 30));
		this.loser.setHorizontalAlignment(JLabel.CENTER);
		this.loser.setPreferredSize(new Dimension(130, 50));

		okBtn.setBorder(BorderFactory.createLineBorder(Color.black, 3));
		okBtn.setPreferredSize(new Dimension(300, 50));
		okBtn.addActionListener(e -> {
			dispose();
		});

		add(panel);
		panel.add(label);
		panel.add(label2);
		panel.add(this.winner);
		panel.add(this.loser);
		add(okBtn, BorderLayout.SOUTH);

		setBounds(frame.getX() + 200, frame.getY() + 250, 300, 300); // JFrame의 중간에 대화상자 띄움
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("경기 결과");
		setModal(true);
		setResizable(false);
		setVisible(true);
	}
}
/**
 * 랭킹을 보여주는 대화상자
 * @author 홍동우(hdw9413@naver.com)
 *
 */
class MyDialog2 extends JDialog {
	private JTable table;
	private MyTableModel mtm;
	private JScrollPane sp;
	
	private JButton okBtn;  //종료하기 버튼
	private JButton regame; //재대결하기 버튼
	private JButton newgame;//새게임하기 버튼

	private JPanel panel;
	private JLabel label = new JLabel("RANK"); //RANK 라는 제목 레이블

	private HashMap<String, User> map;
	public MyDialog2(JFrame frame, HashMap<String, User> map) {
		super(frame);
		this.map = map;
		regame = new JButton("재대결");
		newgame = new JButton("새게임");
		okBtn = new JButton("게임종료");

		MyTetris.gameStat = 3;    //x버튼 누르면 프레임이 안사라짐
		okBtn.addActionListener(e -> { //종료하기 
			MyTetris.gameStat = 2;
			dispose();
		});
		regame.addActionListener(e -> {//다시하기
			MyTetris.gameStat = 1;
			dispose();
		});
		newgame.addActionListener(e -> {//새게임
			MyTetris.gameStat = 0;
			dispose();
		});
		
		
		mtm = new MyTableModel();
		table = new JTable(mtm);
		sp = new JScrollPane(table);
		sp.setBorder(BorderFactory.createLineBorder(Color.black, 3));
		panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 3));

		label.setFont(new Font("serif", Font.BOLD, 30));
		label.setBorder(BorderFactory.createLineBorder(Color.black, 3));
		label.setHorizontalAlignment(JLabel.CENTER);

		setLayout(new BorderLayout());

		add(label, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
		add(panel, BorderLayout.SOUTH);

		panel.add(newgame);
		panel.add(regame);
		panel.add(okBtn);

		setBounds(frame.getX() + 200, frame.getY() + 250, 300, 300); // JFrame의 중간에 대화상자 띄움
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("RANK");
		setResizable(false);
		setModal(true);
		setVisible(true);
	}
	/**
	 * 테이블 모델 객체
	 * 
	 * @author 홍동우(hdw9413@naver.com)
	 *
	 */
	class MyTableModel extends AbstractTableModel {
		private String[] colNames = { "순위", "이름", "승", " 패", "승률" };
		private static final int ROWS = 9;
		private static final int COLS = 5;
		Object[][] data = new String[ROWS][COLS];
		HashMap<String,User> hm; // 유저 정보를 가지고 있는 맵

		
		public MyTableModel() {
			this.hm = map;
			fillTable();
		}
		public void fillTable() {
			List<String> list = sort(map);
			for(int i=0; i<list.size();i++) {
				if( i >= 9 ) {
					break;
				}
				String e = list.get(i);
				data[i][0] = (i+1)+"등";
				data[i][1] = map.get(e).name;
				data[i][2] = "" + map.get(e).winNum;
				data[i][3] = "" + map.get(e).loseNum;
				data[i][4] = "" + String.format("%.1f", map.get(e).winRate);
			}
			
			fireTableDataChanged();
		}
		
		/* map을 정렬하기 위해 Comparator 구현을 위해 만든 함수 */
		public List<String> sort(Map<String, User> map) {
			ArrayList<String> list = new ArrayList<String>();
			//System.out.println("맵크기"+map.size());
			list.addAll(map.keySet());

			Collections.sort(list, (o1, o2) -> {
				User v1 = (User) map.get(o1);
				User v2 = (User) map.get(o2);

				if (v2.winNum > v1.winNum)
					return 1;
				else if (v2.winNum == v1.winNum) { //승수가 같을경우 승률로 순위 정함
					 if( v2.winRate > v1.winRate)
						 return 1;
					 else
						 return -1;
				}
				else
					return -1;
			});

			return list;
		}
		@Override
		public int getRowCount() {
			return ROWS;
		}

		@Override
		public int getColumnCount() {
			return COLS;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		public String getColumnName(int col) {
			return colNames[col];
		}

	}

}



/**
 * 배드블럭 처리 객체(동기화)
 * 
 * @author 홍동우(hdw9413@naver.com)
 *
 */
class BadBlock implements Runnable {
	Queue<String> q; // 배드블럭을 관리할때 사용하는 큐
	MyTetris p1; // p1 패널
	MyTetris p2; // p2 패널
	User p1info; // p1의 유저정보 불러오기 위함
	User p2info; // p2의 유저정보 불러오기 위함
	HashMap<String, User> map; // 유저의 이름과, 그에 관련한 정보를 저장하기 위한 map
	ObjectInputStream in = null; // 파일에서 객체를 불러오기 위한 스트림
	ObjectOutputStream out = null;// 파일에서 객체를 쓰기위한 스트림

	@SuppressWarnings("unchecked")
	public BadBlock(Queue<String> q, MyTetris p1, MyTetris p2) {
		this.q = q;
		this.p1 = p1;
		this.p2 = p2;

		try {
			/* 특정파일이 존재할 경우 그 파일에서 유저정보를 담고 있는 map 을 불러옴 */
			in = new ObjectInputStream(new FileInputStream("TetrisData.dat"));
			map = (HashMap<String, User>) in.readObject();
			load();

		} catch (FileNotFoundException e) {
			/* 파일이 없을 경우 새로 만들고 map도 새로 만든다. */
			System.out.println("파일이 없어서 생성함");
			map = new HashMap<String, User>();
			load();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void load() {
		/* p1의 이름에 해당하는 User객체가 map에 들어있으면 불러옴 */
		if (map.containsKey(p1.getName())) {
			p1info = map.get(p1.getName());
			System.out.println(p1.getName() + "정보 불러오기 성공");
		} else {
			p1info = new User(p1.getName());
			map.put(p1.getName(), p1info);
			System.out.println(p1.getName() + " map에 없어서 새로 만듬");
		}

		/* p2의 이름에 해당하는 User객체가 map에 들어있으면 불러옴 */
		if (map.containsKey(p2.getName())) {
			p2info = map.get(p2.getName());
			System.out.println(p2.getName() + "정보 불러오기 성공");
		} else {
			p2info = new User(p2.getName());
			map.put(p2.getName(), p2info);
			System.out.println(p2.getName() + " map에 없어서 새로 만듬");
		}

		/* 디버그용 (맵 순회 하기 ) 
		map.forEach((key, value) -> {
			System.out.println(
					"key=" + key + ", win=" + value.winNum + ", lose=" + value.loseNum + ", winRate=" + value.winRate);
		});*/
	}

	/*
	 * 배드블럭 큐에 써질때까지 기다렸다가 써지면 쓴 객체 상대편에 배드블럭을 추가 시킨다.
	 */
	public void run() {
		/* 동기화 블럭 */

		synchronized (q) {
			while (MyTetris.running) {
				try {
					/* Queue 에 데이터가 써질떄까지 기다린다. */
					while (q.isEmpty()) {
						q.wait();
					}
				} catch (InterruptedException e) { // 패널이 모두 정지하고 인터럽트 신호 걸면 while문 종료
					break;
				}
				int size = q.size();
				for (int i = 0; i < size; i++) {
					if (q.peek().equals(p1.getName())) { // 큐에 p1의 이름이 써있으면 p2의 블럭 증가
						q.remove();
						p2.addRow();

					} else { // 큐에 p2의 이름이 써있으면 p1의 블럭증가
						q.remove();
						p1.addRow();
					}
				}
				q.notifyAll();
			}
		}

		/* 게임 끝나고 처리할 데이터 정리 */
		System.out.println("데이터를 정리합니다.");
		/* p1이 승자라면 */
		if (p1.isWinner) {
			p1info.winNum++;  //승수 올리고
			p1info.computeWinRate(); //승률계산

			p2info.loseNum++;
			p2info.computeWinRate();
		} else { // p2가 승자라면
			p2info.winNum++;
			p2info.computeWinRate();

			p1info.loseNum++;
			p1info.computeWinRate();
		}

		/* map 에 유저정보 저장 */
		map.put(p1.getName(), p1info);
		map.put(p2.getName(), p2info);

		/* 파일에 map객체 쓰기 */
		try {
			out = new ObjectOutputStream(new FileOutputStream("TetrisData.dat"));
			out.writeObject(map);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(Thread.currentThread().getName() + "종료");
	}

	

}

/**
 * 유저의 정보를 표현하는 클래스(공용체)
 */
class User implements Serializable {
	private static final long serialVersionUID = 1L;

	public String name;
	public int winNum;
	public int loseNum;
	public double winRate;

	/* 처음엔 0승 0패 승률 0% */
	public User(String name) {
		this.name = name;
		winNum = 0;
		loseNum = 0;
		winRate = 0.0;
	}
	/* 승률 계산 함수 */
	public void computeWinRate() {
		winRate = (winNum * 1.0) / (winNum + loseNum) * 100;
	}

}

/** 블럭의 모양과 색깔등의 정보를 담고 있는 클래스(공용체) */
class Block {
	public static final Color[] color = { Color.yellow, new Color(0xFF00FF), Color.orange, Color.green,
			new Color(0x00FFFF), Color.RED, Color.blue };

	public static final Point[][][] form = {
			// 네모
			{ { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) } },
			// T자 모양
			{ { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 1) },
					{ new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 1) } },
			// 'ㄱ'자 모양
			{ { new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
					{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) } },
			// 'S'자 모양
			{ { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(0, 2) },
					{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(0, 2) }

			},
			// 막대기
			{ { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) } },
			// 'Z'자 모양
			{ { new Point(0, 1), new Point(1, 0), new Point(1, 1), new Point(2, 0) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
					{ new Point(0, 1), new Point(1, 0), new Point(1, 1), new Point(2, 0) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }

			},
			// 'ㄴ'자 모양
			{ { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
					{ new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(1, 2) } } };
}