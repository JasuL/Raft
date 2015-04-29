package edu.duke.raft;

import java.util.Timer;
public class LeaderMode extends RaftMode {
	private Timer myHeartbeatTimer;
	private int HEARTBEAT_TIMER_ID = 3;
	//TODO Another timer for checking if recent thing committed.
  public void go () {
	
    synchronized (mLock) {
    
      System.out.println ("S" + 
			  mID + 
			  "." + 
			  mConfig.getCurrentTerm() + 
			  ": switched to leader mode.");
      
      this.sendHeartbeats();
  	Timer myHeartbeatTimer = scheduleTimer(this.HEARTBEAT_INTERVAL, this.HEARTBEAT_TIMER_ID);
      
    }
  }
  private void sendHeartbeats(){
		int term = mConfig.getCurrentTerm();
		Entry heartbeatEntry = null;
		Entry[] entries = {heartbeatEntry};
		for(int i=1;i<=mConfig.getNumServers();i++){
			if(i==mID){
				continue;
			}
			System.out.println(mID + " SENDING HEARTBEAT");
			remoteAppendEntries(mID, term, mID, mLog.getLastIndex(), mLog.getLastTerm(),entries ,mCommitIndex);
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
      int term = mConfig.getCurrentTerm ();
      int vote = term;
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
      if(leaderTerm>term){
    	  this.myHeartbeatTimer.cancel();
    	  RaftServerImpl.setMode(new FollowerMode());
    	  return 0;
      }
      return term;
    }
  }

  // @param id of the timer that timed out
  public void handleTimeout (int timerID) {
    synchronized (mLock) {
    	if(timerID==this.HEARTBEAT_TIMER_ID){
    		this.sendHeartbeats();
    	}
    }
  }
}
