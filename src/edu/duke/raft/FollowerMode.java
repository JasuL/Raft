package edu.duke.raft;

import java.util.Random;
import java.util.Timer;
public class FollowerMode extends RaftMode {
	private Timer myTimer;
	public void go () {
		synchronized (mLock) {
			int term = 0;
			System.out.println ("S" + 
					mID + 
					"." + 
					term + 
					": switched to follower mode.");
			Random rand = new Random();
			myTimer = scheduleTimer(rand.nextInt(this.ELECTION_TIMEOUT_MAX - this.ELECTION_TIMEOUT_MIN) + this.ELECTION_TIMEOUT_MIN, this.mID); //may need to change timer id here

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
			myTimer.cancel(); //Not sure if we should cancel the timer here
			System.out.println("Vote requested from follower server " + mID);
			//Vote for candidate if it has at least as up to date term
			//Vote for candidate if it has at least as up to date entries (TODO)
			int term = mConfig.getCurrentTerm ();
			int vote = term;
			if(lastLogTerm>=term){
				//Additional checks to be added for log status
				System.out.println("Follower " + mID + " voting for candidate " + candidateID);
				return 0;
			}
			return vote;
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
		synchronized (mLock) {
			int term = mConfig.getCurrentTerm ();
			int result = term;
			return result;
		}
	}  

	// @param id of the timer that timed out
	public void handleTimeout (int timerID) {
		synchronized (mLock) {
			myTimer.cancel();
			System.out.println(timerID + " has detected the leader has TIMED OUT\n");
			RaftServerImpl.setMode(new CandidateMode()); //Not exactly sure how this line works...
		}
	}
}

