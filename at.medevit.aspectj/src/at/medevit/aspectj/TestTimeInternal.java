package at.medevit.aspectj;

public class TestTimeInternal {
	
	public static void main(String[] args){
		asyncExec();
	}
	
	public static void asyncExec(){
		System.out.println("... inside an async exec");
	}
}
