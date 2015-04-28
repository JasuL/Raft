package edu.duke.raft;

import java.util.Random;
import java.util.Timer;
public class FollowerMode extends RaftMode {
	private Timer myLeaderTimeoutTimer;
	private int LEADER_TIMEOUT_TIMER_ID = 1;
	private int lastElectionTerm = 0;
	public void go () {
		synchronized (mLock) {
			System.out.println ("S" + 
					mID + 
					"." + 
					mConfig.getCurrentTerm() + 
					": switched to follower mode.");
			Random rand = new Random();
			myLeaderTimeoutTimer = scheduleTimer(rand.nextInt(this.ELECTION_TIMEOUT_MAX - this.ELECTION_TIMEOUT_MIN) + this.ELECTION_TIMEOUT_MIN, this.LEADER_TIMEOUT_TIMER_ID); //may need to change timer id here

		}
	}
	private void incrementTerm(int newTerm){
		//mConfig.setVotedFor(0);
		//
	}
	private void resetLeaderTimeoutTimer(){
		myLeaderTimeoutTimer.cancel();
		Random rand = new Random();
		myLeaderTimeoutTimer = scheduleTimer(rand.nextInt(this.ELECTION_TIMEOUT_MAX - this.ELECTION_TIMEOUT_MIN) + this.ELECTION_TIMEOUT_MIN, this.mID);
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
			if(this.lastElectionTerm<candidateTerm){ //check if this is a new election, if so, reset my votedFor
				mConfig.setCurrentTerm(term, 0);
				this.lastElectionTerm=candidateTerm;
			}
			myLeaderTimeoutTimer.cancel(); //Not sure if we should cancel the timer here
			//Vote for candidate if it has at least as up to date term
			//Vote for candidate if it has at least as up to date entries (TODO)
			
			if(candidateTerm>=term && mConfig.getVotedFor()==0){ //Candidate has an up  to date term and I have not voted yet. // 
				//Additional checks to be added for log status
				mConfig.setCurrentTerm(term, candidateID); //set who we voted for, but do not increment term yet.
				return 0; //Vote for the candidate
			}
			return term;
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
		System.out.println(mID + " HEARTBEAT");
		synchronized (mLock) {
			System.out.println(mID + " HEARTBEAT");
			this.resetLeaderTimeoutTimer();
			int term = mConfig.getCurrentTerm();
			if(leaderTerm>=term){
				mConfig.setCurrentTerm(leaderTerm, 0);
				mLastApplied=mLog.append(entries);
				return 0;
			}
			return term;
		}
	}  

	// @param id of the timer that timed out
	public void handleTimeout (int timerID) {
		synchronized (mLock) {
			myLeaderTimeoutTimer.cancel();
			System.out.println(mID + " has detected the leader has TIMED OUT\n");
			RaftServerImpl.setMode(new CandidateMode()); //Not exactly sure how this line works...
		}
	}
}

