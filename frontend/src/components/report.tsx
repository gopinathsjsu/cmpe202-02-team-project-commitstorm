import { useEffect, useState } from "react";
import Post from './post';
import type { ListingDetailProps } from "./post";
import { getListingById } from "../services/listingsService";
import { getUserById, updateUserStatus } from "../services/userService";
import { assignModerator, updateReportStatus } from "../services/reportsService";


function Report(props: {userId: string, reporterId: string, postId: string, reportId: string}) {
  const [post, setPost] = useState<ListingDetailProps | null>(null);
  const [reportedId, setReportedId] = useState('');
  const [reporterName, setReporterName] = useState('');

  async function fetch () {
    try{
      const data = await getListingById(props.postId);
      setPost(data as ListingDetailProps);
      setReportedId(post? post.userId: '');
      const response = await getUserById(props.reporterId);
      const name = response.name;
      setReporterName(name);
    }
    catch (e){
      console.log("Error: "+e)
    }
  }

  async function assign() {
    try{
      await assignModerator(props.userId, props.reportId);
    }
    catch (e){
      console.log("Error assigning admin to report");
    }
  }

  async function closeReport(status: string) {
    try{
      const response = await updateReportStatus(props.reportId, status);
      if(response.status == 200){
        console.log("Closed Report");
      }
      else{
        console.log( "Could not close report");
      }
    } catch (e) {
      console.log("Error closing report");
    }
  }

  useEffect(() =>  {
    fetch();
    
  },[props]);


  const handleBan = async () => {
    assign();
    try{
      const response = await updateUserStatus(reportedId, 'BANNED')
      if (response.status == 200){
        closeReport("ACTIONED");
        console.log("User banned");
        fetch();
      }
      else{
        console.log("Failed to ban "+ reportedId)
      }
    } catch (e){
      console.log("Error banning: "+ e);
    }
  };

  const handleSuspend = async () => {
    assign();
    try{
      const response = await updateUserStatus(reportedId, 'SUSPENDED')
      if (response.status == 200){
        closeReport("ACTIONED");
        console.log("User suspended");
        fetch();
      }
      else{
        console.log("Failed to ban "+ reportedId)
      }
    } catch (e){
      console.log("Error banning: "+ e);
    }
  };

  const handleDeny = async () => {
    assign();
    closeReport("RESOLVED");
    fetch();
  };

  return (
    <div style={{ backgroundColor: 'white', borderRadius: '10px', padding: '10px' }}>
      <div style={{ marginBottom: '10px', color: 'black' }}>
        <strong>Reporter:</strong> {reporterName}
      </div>
      <div style={{ marginBottom: '10px', color: 'black' }}>
       <strong>Image:</strong> <img src={post?.imageUrl} alt="Post" style={{ maxWidth: '100px', borderRadius: '10px', marginBottom: '10px' }}/>
      </div>
      <div style={{ marginBottom: '10px', color: 'black' }}>
        <strong>Description:</strong> {post?.description}
      </div>
      <div style={{ marginBottom: '10px', color: 'black' }}>
        <strong>Category:</strong> {post?.categoryName}
        <div><strong>Condition:</strong> {post?.condition}</div>
      </div>
      <div style={{ marginBottom: '10px', color: 'black' }}>
        <strong>Price:</strong> ${post?.price}
      </div>
      <button onClick={handleSuspend} style={{ margin: '5px', padding: '10px', backgroundColor: 'orange', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
        Suspend
      </button>
      <button onClick={handleBan} style={{ margin: '5px', padding: '10px', backgroundColor: 'red', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
        Ban
      </button>
      <button onClick={handleDeny} style={{ margin: '5px', padding: '10px', backgroundColor: 'gray', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
        Deny
      </button>
    </div>
  );
}

export default Report;