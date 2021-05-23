import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WoodCalculator {
	
	public static void main(String[] args) throws IOException {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		List<Wood> src = new ArrayList<Wood>();
		List<Request> reqs = new ArrayList<Request>();
		
		FileWriter fileWriter = new FileWriter("Result.txt");
		try {
			
			readWoodType(src);
			readRequest(reqs);
			
			printWoodType(src, fileWriter);
			printRequests(reqs, fileWriter);
			
			List<Result> results = new ArrayList<Result>();
			
			while(keepCut(reqs)) {
				Result result = getCutResult(src, reqs);
				results.add(result);
				
				// do cut in requests
				for(Wood c : result.contains) {
					for(Request req : reqs) {
						if(c.length == req.wood.length && c.width == req.wood.width) {
							req.count--;
							break;
						}
					}
				}
			}
			
			printResult(src, results, fileWriter);
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			fileWriter.write("File not found.\n");
			fileWriter.write(e.getMessage() + "\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException.");
			fileWriter.write("IOException.\n");
			fileWriter.write(e.getMessage() + "\n");
			e.printStackTrace();
		} finally {
			fileWriter.close();
		}
	}
	
	private static void printResult(List<Wood>src, List<Result> results, FileWriter fileWriter) throws IOException {
		Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
		
		fileWriter.write("\nResults----------------------------------------------------\n\n");
		for(Result r : results) {
			if(resultMap.get(r.type) == null) {
				resultMap.put(r.type, 1);
			} else {
				resultMap.put(r.type, resultMap.get(r.type) + 1);
			}
			
			String output = "Type: " + src.get(r.type).length + " / " + src.get(r.type).width;
			System.out.println(output);
			fileWriter.write(output + "\n");
			for(Wood w : r.contains) {
				output = "Contains: " + w.length + " /"  + w.width;
				System.out.println(output);
				fileWriter.write(output + "\n");
			}
			for(Wood w : r.remains) {
				output = "Remains: " + w.length + " / " + w.width;
				System.out.println(output);
				fileWriter.write(output + "\n");
			}
			System.out.println("----------------------------------------------------");
			fileWriter.write("----------------------------------------------------\n");
		}
		
		System.out.println("Total Need:");
		fileWriter.write("\n Total Need: \n");
		for(int i = 0; i < src.size(); i++) {
			String output = "Type: " + src.get(i).length + " / " + src.get(i).width + ": " + resultMap.get(i) + " pieces";
			System.out.println(output);
			fileWriter.write(output + "\n");
		}
	}
	
	private static Result getCutResult(List<Wood> src, List<Request> reqs) {
		List<Result> tempResult = new ArrayList<Result>();
		for(int type = 0; type < src.size(); type++) {
			List<Request> tempReqs = new ArrayList<Request>(reqs);
			Result r = new Result(type);
			r.remains.add(src.get(type));
			
			while(cut(tempReqs, r));
			tempResult.add(r);
		}
		
		int smallIndex = 0;
		int smallSum = Integer.MAX_VALUE;
		for(int i = 0; i < tempResult.size(); i++) {
			Result tr = tempResult.get(i);
			int tempSmallSum = 0;
			for(Wood remain : tr.remains) {
				tempSmallSum += remain.length*remain.width;
			}
			if(tempSmallSum < smallSum) {
				smallSum = tempSmallSum;
				smallIndex = i;
			}
		}
		
		return tempResult.get(smallIndex);
	}
	
	private static void readWoodType(List<Wood> src) throws FileNotFoundException {
		// read wood Type
		File file = new File("WoodType.txt");
		Scanner reader = new Scanner(file);
		while (reader.hasNextLine()) {
			String data = reader.nextLine();
			int value1 = Integer.valueOf(data.split(" ")[0]);
			int value2 = Integer.valueOf(data.split(" ")[1]);
			src.add(new Wood(Math.max(value1, value2), Math.min(value1, value2)));
		}
		reader.close();
	}
	
	private static void readRequest(List<Request> reqs) throws FileNotFoundException {
		File file = new File("Request.txt");
		Scanner reader = new Scanner(file);
		while (reader.hasNextLine()) {
			String data = reader.nextLine();
			int count = Integer.valueOf(data.split(" ")[0]);
			int value1 = Integer.valueOf(data.split(" ")[1]);
			int value2 = Integer.valueOf(data.split(" ")[2]);
			reqs.add(new Request(count, new Wood(Math.max(value1, value2), Math.min(value1, value2))));
		}
		reader.close();
		reqs.sort(new RequestComparator());
	}
	
	private static void printWoodType(List<Wood> src, FileWriter myWriter) throws IOException {
		myWriter.write("\nWood Types----------------------------------------------------\n\n");
		for(Wood w : src) {
			String output = w.length + " x " + w.width;
			System.out.println(output);
			myWriter.write(output + "\n");
		}
	}
	
	private static void printRequests(List<Request> reqs, FileWriter fileWriter) throws IOException {
		fileWriter.write("\nRequests----------------------------------------------------\n\n");
		for(Request r : reqs) {
			String output = r.wood.length + " x " + r.wood.width + "     - " + r.count + " pieces";
			System.out.println(output);
			fileWriter.write(output + "\n");
		}
	}
	
	private static boolean cut(List<Request> reqs, Result r) {
		for(Wood remain : r.remains) {
			for(Request req : reqs) {
				if(req.count > 0 && remain.length >= req.wood.length && remain.width >= req.wood.width) {
					r.remains.remove(remain);
					int edg1 = req.wood.length;
					int edg2 = remain.width - req.wood.width;
					Wood piece1 = new Wood(Math.max(edg1, edg2), Math.min(edg1, edg2));
					r.remains.add(piece1);
					
					edg1 = remain.width;
					edg2 = remain.length - req.wood.length;
					Wood piece2 = new Wood(Math.max(edg1, edg2), Math.min(edg1, edg2));
					r.remains.add(piece2);
					
					r.contains.add(req.wood);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static boolean keepCut(List<Request> reqs) {
		for(Request r : reqs) {
			if(r.count > 0) {
				return true;
			}
		}
			
		return false;
	}
	
	private static class Wood {
		public int length;
		public int width;
		
		public Wood(int length, int width) {
			this.length = length;
			this.width = width;
		}
	}
	
	private static class Request {
		public int count;
		public Wood wood;
		
		public Request(int count, Wood wood) {
			this.count = count;
			this.wood = wood;
		}
	}
	
	private static class Result {
		public int type;
		public List<Wood> contains = new ArrayList<Wood>();
		public List<Wood> remains = new ArrayList<Wood>();
		
		public Result(int type) {
			this.type = type;
		}
	}
	
	private static class RequestComparator implements Comparator<Request> {
		@Override
		public int compare(Request req1, Request req2) {
			if(req2.wood.length - req1.wood.length == 0) {
				return req2.wood.width - req1.wood.width;
			}
			return req2.wood.length - req1.wood.length;
		}
	}
}
