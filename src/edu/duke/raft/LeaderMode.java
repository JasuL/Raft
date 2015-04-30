package edu.duke.raft;

import java.util.Timer;
import java.util.Arrays;
import java.util.ArrayList;

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
          Timer myHeartbeatTimer = scheduleTimer(this.HEARTBEAT_INTERVAL, this.HEARTBEAT_TIMER_ID); //start heartbeat timer

      }
  }

  private void sendHeartbeats(){

    System.out.println("Leader "+mID + " sending HEARTBEAT");
     repairLog(); //repair other server logs to mathch leader log

     // old heartbeatcode
//		int term = mConfig.getCurrentTerm();
//		Entry heartbeatEntry = null;
//		Entry[] entries = {heartbeatEntry};
//		for(int i=1;i<=mConfig.getNumServers();i++){
//			if(i==mID){
//				continue;
//			}
//			System.out.println(mID + " SENDING HEARTBEAT");
//			remoteAppendEntries(i, term, mID, mLog.getLastIndex(), mLog.getLastTerm(),entries ,mCommitIndex);
//		}

 }

//CHECK OVER THIS LOGIC. LOOKS FUCKED UP
 private void repairLog(){
   RaftResponses.setTerm(mConfig.getCurrentTerm());

     int[] latestMatchingIndex = new int[6]; //maintain last matching logs of leader and each server ,  WHY 6??
     int response = -1; //failed match logging

     Arrays.fill(latestMatchingIndex, mLog.getLastIndex()); //fill intially assuming all server logs are equal length to leader

     //iterate through servers
     for(int j=1;j<=mConfig.getNumServers();j++){

        while(response!=0){ //if indices dont match
         ArrayList<Entry> entryList = new ArrayList<Entry>();

         for(int i=latestMatchingIndex[j];i < mLog.getLastIndex()+1;i++){
          entryList.add(mLog.getEntry(i));
      }

      Entry[] entries = new Entry[entryList.size()];
      entries = entryList.toArray(entries);
      remoteAppendEntries(j, mConfig.getCurrentTerm(), mID, latestMatchingIndex[j], mLog.getEntry(latestMatchingIndex[j]).term, entries ,mCommitIndex);

          latestMatchingIndex[j]--; //decrement log index and retry

          int[] responses = RaftResponses.getAppendResponses(mConfig.getCurrentTerm());
			  response = responses[j]; //might be off by one
          }
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
      if (candidateTerm>term){
        this.myHeartbeatTimer.cancel();
        RaftServerImpl.setMode(new FollowerMode()); // Revert to follower if candidate has larger term than current leader
        return 0;
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
    synchronized (mLock) {

        //shouldnt get here because this is current leader
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
    myHeartbeatTimer.cancel();
        myHeartbeatTimer = scheduleTimer(this.HEARTBEAT_INTERVAL, this.HEARTBEAT_TIMER_ID); //reset heartbeat timer
        this.sendHeartbeats();
    }
}
}
}
