package main;

import java.util.Random;

public class Magic8Ball {

	public static void main(String[] args) {
		System.out.println(shake());
	}

	private static String[] answers = {
			"It is certain",
			"It is decidedly so",
			"Without a doubt",
			"Yes definitely",
			"You may rely on it",
			"As I see it, yes",
			"Most likely",
			"Outlook good",
			"Yes",
			"Signs point to yes",
			"Reply hazy, try again",
			"Ask again later",
			"Better not tell you now",
			"Cannot predict now",
			"Concentrate and ask again",
			"Don't count on it",
			"My reply is no",
			"My sources say no",
			"Outlook not so good",
			"Very doubtful"
	};


	private static class ComputeThread extends Thread {
		@Override
		public void run() {
			long start = System.currentTimeMillis();

			while(true) {
				if(System.currentTimeMillis()-start > (20*1000)) break;

				Math.log(Math.cos(System.currentTimeMillis()));
			}
		}
	}


	public static String shake() {

//		for(int i=0; i<Runtime.getRuntime().availableProcessors(); i++)
//			new ComputeThread().start();

		int n = new Random().nextInt(answers.length);

		return answers[n];
	}

}
