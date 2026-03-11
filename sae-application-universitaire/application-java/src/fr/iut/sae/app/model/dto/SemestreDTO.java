package fr.iut.sae.app.model.dto;

public class SemestreDTO {
	private int numSemestre;
	private int nombreMaxCovoit;
	private int estApprentissage; // 0/1

	public int getNumSemestre() { return numSemestre; }
	public void setNumSemestre(int numSemestre) { this.numSemestre = numSemestre; }

	public int getNombreMaxCovoit() { return nombreMaxCovoit; }
	public void setNombreMaxCovoit(int nombreMaxCovoit) { this.nombreMaxCovoit = nombreMaxCovoit; }

	public int getEstApprentissage() { return estApprentissage; }
	public void setEstApprentissage(int estApprentissage) { this.estApprentissage = estApprentissage; }	
		
	@Override
	public String toString() {
		return "S" + numSemestre;
	}
}
