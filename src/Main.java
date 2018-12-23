public class Main {
	
	public static void main(String[] args) {

		LCADataset nuclearDataset = new LCADataset("resources/nuclear_power.xml");
		LCADataset biomassDataset = new LCADataset("resources/biomass_power.xml");

		String name = "Cobalt-57";

		InOutValue inOutBiomass = biomassDataset.getInOutValue(name);
		InOutValue inOutNuclear = nuclearDataset.getInOutValue(name);
		
		System.out.println(inOutBiomass.toDescriptionString());
		System.out.println(inOutNuclear.toDescriptionString());
	}
}