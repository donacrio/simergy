/*
 * @author Donatien Criaud
 * 
 */
package simergy.core.resources;

import java.util.ArrayList;
import simergy.core.patients.*;
import simergy.core.events.EventState;
import simergy.core.distributions.ConsultationDistribution;

/**
 * The Class Physician.
 * 
 * This class represents is used to represent a physician.
 * A physician has patients to see, overseen patients and treated patient.
 * A patient to see is the "waiting queue" of the physician whereas an overseen patient is a patient attributed to the physician but not yet treated.
 */
public class Physician extends HumanResource{

	private static final long serialVersionUID = -7677139446633853275L;
	
	private ArrayList<Patient> overseenPatients;
	private ArrayList<Patient> toBeSeenPatients;
	private ArrayList<Patient> treatedPatients;
	private Patient currentPatient;
	
	/**
	 * Instantiates a new physician.
	 *
	 * @param name the physician's name
	 * @param surname the physician's surname
	 */
	public Physician(String name, String surname) {
		super(name, "PHYSICIAN", surname);
		this.overseenPatients = new ArrayList<Patient>();
		this.treatedPatients = new ArrayList<Patient>();
		this.toBeSeenPatients = new ArrayList<Patient>();
	}
	
	/**
	 * Sets the prescription of a patient.
	 *
	 * @param patient the patient
	 */
	public void givePrescription(Patient patient, boolean secondTime) {
		if(!secondTime){
			patient.setPrescription(new ConsultationDistribution().generatePrescription());
		}else{
			patient.setPrescription("NONE");
		}
		
	}
	
	/**
	 * Sets the outcome of a patient.
	 *
	 * @param patient the patient
	 */
	public void giveOutcome(Patient patient) {
		patient.setOutcome("RELEASE");
	}
	
	/**
	 * Adds a new patient to the physician.
	 * Technically, the patient is added to the overseen patients and to the patients to see
	 * If the patient is at a critical severity level, it is set as the current patient
	 *
	 * @param patient the patient
	 */
	public void addPatient(Patient patient){
		if(patient.getSeverityLevel()==SeverityLevel.L1 || patient.getSeverityLevel()==SeverityLevel.L2){
			overseenPatients.add(patient);
			toBeSeenPatients.add(0,patient);
			patient.setPhysician(this);
			if(currentPatient!=null && currentPatient.getSeverityLevel()!=SeverityLevel.L1 
					&& currentPatient.getSeverityLevel()!=SeverityLevel.L2){
				currentPatient.getWorkflow().getCurrentEvent().setState(EventState.NS);
				currentPatient.getWorkflow().getCurrentEvent().setStartTime(0);
				currentPatient.getWorkflow().getCurrentEvent().setEndTime(0);
				currentPatient.getWorkflow().getCurrentEvent().getRessources().put("PHYSICIAN",null);
				currentPatient.setState(PatientState.W);
				currentPatient = patient;
				state = State.IDLE;
			}
			
		}else{
			overseenPatients.add(patient);
			toBeSeenPatients.add(patient);
			patient.setPhysician(this);
		}
		if(currentPatient==null){
			currentPatient = patient;
		}

	}
	
	/**
	 * Notify the physician.
	 * 
	 * Adds a patient to the patients to see
	 *
	 * @param patient the patient
	 */
	public void notify(Patient patient){
		toBeSeenPatients.add(patient);
	}
	
	/**
	 * Returns the next patient in the waiting queue
	 * 
	 * @param patient the patient
	 * 
	 * @see simergy.core.resources.Resource#nextPatient()
	 */
	@Override
	public Patient nextPatient(){
		return currentPatient;
	}
	
	/**
	 * Removes a patient from the physician's patients to see.
	 *
	 * @param patient the patient
	 */
	public void patientSeen(Patient patient){
		toBeSeenPatients.remove(patient);
		currentPatient = toBeSeenPatients.size()==0?null:toBeSeenPatients.get(0);
	}
	
	/**
	 * Adds a patient to the treated patients.
	 * Removes this patient from the overseen patients;
	 *
	 * @param patient the patient
	 */
	public void patientTreated(Patient patient){
		overseenPatients.remove(patient);
		treatedPatients.add(patient);
	}

/*Getters and Setters*/
	
	/**
 * Gets the overseen patients.
 *
 * @return the overseen patients
 */
public ArrayList<Patient> getOverseenPatients() {
		return overseenPatients;
	}

	/**
	 * Gets the to be seen patients.
	 *
	 * @return the to be seen patients
	 */
	public ArrayList<Patient> getToBeSeenPatients() {
		return toBeSeenPatients;
	}

	/**
	 * Gets the treated patients.
	 *
	 * @return the treated patients
	 */
	public ArrayList<Patient> getTreatedPatients() {
		return treatedPatients;
	}
	
}
