package edu.duke.raft;

import java.util.Timer;
import java.util.Random;
public class CandidateMode extends RaftMode {
	private Timer myElectionTimeoutTimer;
	private int ELECTION_TIMEOUT_TIMER_ID = 2;
	public void go () {
		synchronized (mLock) {
			this.incrementTerm();    
			System.out.println ("S" + 
					mID + 
					"." + 
					mConfig.getCurrentTerm() + 
					": switched to candidate mode.");
			this.beginElection();
		}
		

	}
	private void incrementTerm(){
		mConfig.setCurrentTerm(mConfig.getCurrentTerm()+1,0);
	}
	private void beginElection(){
		System.out.println("Candidate "+mID+" starting election.");
		RaftResponses.setTerm(mConfig.getCurrentTerm());
		RaftResponses.clearVotes(mConfig.getCurrentTerm());
		Random rand = new Random();
		myElectionTimeoutTimer = scheduleTimer(rand.nextInt(this.ELECTION_TIMEOUT_MAX - this.ELECTION_TIMEOUT_MIN) + this.ELECTION_TIMEOUT_MIN, this.ELECTION_TIMEOUT_TIMER_ID);
		for(int i=1;i<=mConfig.getNumServers();i++){
			this.remoteRequestVote(i, mConfig.getCurrentTerm(), this.mID, mLastApplied, mLog.getLastTerm()); 
		}
	}

	// @param candidate’s term
	// @param candidate requesting vote
	// @param index of candidate’s last log entry
	// @param term of candidate’s last log entry
	// @return 0, if server votes for candidate; otherwise, server's
	// current term 
	public int requestVote (int candidateTerm,
			int candidateID,
			int lastLogIndex,
			int lastLogTerm) {
		synchronized (mLock) {
			int term = mConfig.getCurrentTerm();
			System.out.println("Vote requested from serverID: " + mID);	
			if(candidateID==mID){
				return 0;
			}
			else{
				return term;
			}
		}
	}


	// @param leader’s term
	// @param current leader
	// @param index of log entry before entries to append
	// @param term of log entry before entries to append
	// @param entries to append (in order of 0 to append.length-1)
	// @param index of highest committed entry
	// @return 0, if server appended entries; otherwise, server's
	// current term
	public int appendEntries (int leaderTerm,
			int leaderID,
			int prevLogIndex,
			int prevLogTerm,
			Entry[] entries,
			int leaderCommit) {
		//synchronized (mLock) {
			int term = mConfig.getCurrentTerm();
			if(leaderTerm>=term){
				this.myElectionTimeoutTimer.cancel();
				mLastApplied=mLog.append(entries);
				mConfig.setCurrentTerm(leaderTerm,0);
				RaftServerImpl.setMode(new FollowerMode());
				return 0;
			}
			return term;
		//}
	}

	// @param id of the timer that timed out
	//ID=1 Count Votes
	//ID=2 Election has timed out... call a second election.
	public void handleTimeout (int timerID) {
		synchronized (mLock) {
			if(timerID==this.ELECTION_TIMEOUT_TIMER_ID){
				this.myElectionTimeoutTimer.cancel();
				int[] votes = RaftResponses.getVotes(mConfig.getCurrentTerm());
				int voteCounter=0;
				for(int i=0;i<votes.length;i++){
					if(votes[i]==0){
						voteCounter++;
					}
				}
				System.out.println("Counted " + voteCounter + " votes for candidate "+ this.mID);
				if(voteCounter>votes.length/2){
					RaftServerImpl.setMode(new LeaderMode());
				}
				else{
					System.out.println("Election for candidate " + this.mID + " cancelled.");
					this.incrementTerm();
					this.beginElection();
				}
			}
		}
	}
}
