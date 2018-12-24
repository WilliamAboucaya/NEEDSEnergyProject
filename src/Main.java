import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		while (true) {
			System.out.println("Type the name of an element to get its used and produced amounts (or /exit to quit this program):");
			String name = sc.nextLine();
			if (name.equals("/exit")) break;
			System.out.println("...");
			try {
				LCADataset nuclearDataset = new LCADataset("resources/nuclear_power.xml");
				LCADataset biomassDataset = new LCADataset("resources/biomass_power.xml");

				InOutValue inOutBiomass = biomassDataset.getInOutValue(name);
				InOutValue inOutNuclear = nuclearDataset.getInOutValue(name);
		
				System.out.println("\n------------------------------------------------------\n");
				System.out.println("For biomass production:");
				System.out.println(inOutBiomass.toDescriptionString());
				System.out.println("\n------------------------------------------------------\n");
				System.out.println("For nuclear production:");
				System.out.println(inOutNuclear.toDescriptionString());
				System.out.println("\n------------------------------------------------------\n");
			} catch (NoSuchElementException e) {
				System.out.println(e.getMessage());;
			}
		}
		System.out.println("See you :)");
		
		sc.close();
	}
}