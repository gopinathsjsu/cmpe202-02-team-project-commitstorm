import { useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { FileUploader } from "react-drag-drop-files";
import '../css/listingForm.css';
import { postListings } from '../services/listingsService';
import { ToastContainer, toast } from 'react-toastify';
import { getS3Url } from '../services/imageService';

const ListingForm = () => {
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('auth.user') || '{}'));
  const { register, handleSubmit } = useForm<{ title: string; description: string; category: number; condition: string; price: number; }>();
  const categories = ["Arts & Crafts","Automotive","Books","Clothing","Electronics","Electronics & Gadgets","Furniture",
  "Health & Beauty","Home & Garden","Musical Instruments","Office Supplies","Sports","Toys & Games"];
  const conditions = {
    NEW: 'New',
    LIKE_NEW: 'Like New',
    GOOD: 'Good',
    FAIR: 'Fair',
    POOR: 'Poor'
  };

  const categoriyIds = {
    "Appliance": "8b57f97b-8510-4055-b76b-f065203c78b5",
    "Arts & Crafts": "1664ec99-d9c5-491f-b8f1-735a1dcfe860",
    "Automotive": "738e94a7-4f68-4c2c-9f50-9a0217b8562b",
    "Books": "40353a8e-b884-49c2-b12f-682d18db16c0",
    "Clothing": "87227c09-7c13-4212-bb56-ab8eb7268566",
    "Collectibles": "8c869880-3218-44a1-8d73-fef401eb6a6f",
    "Electronics": "109b241c-7f4b-46a8-a2c7-f691828b6df4",
    "Furniture": "8790a73b-3d7d-4bf1-bfe2-9b6148881941",
    "Health & Beauty": "d547a8b3-033f-4d56-8884-aebb331824bc",
    "Home & Garden": "476cc520-0b51-42b8-b852-6ac90a107c8f",
    "Jewelery": "8e5ebac3-8efe-4efa-8b3c-7f044c596906",
    "Musical Instruments": "2f737811-d95a-4d26-89a9-4e8572491f58",
    "Office Supplies": "574ae657-9b54-4f87-b054-b9793c97f440",
    "Sports": "ca1e92e1-aeef-4dc9-8b62-b2dcf17873ab",
    "Toys & Games": "407a1eef-ef45-4959-8618-35e6f6f4f9a0"
  }

  const generateS3Url = async () => {
    let batch = false
    let data = {}
    if (file && file.length > 1){
      batch = true;
      data = file.map(f => ({contentType: f.item(0).type, fileName: f.item(0).name}));

    }else{
      data = {contentType: file[0].item(0).type, fileName: file[0].item(0).name};
    }
    try{
      const response = await getS3Url(data, batch);
      const uploadResponse = await fetch(response.presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': file[0].item((0)).type
        },
        body: file[0].item((0))
      });

      if (!uploadResponse.ok) {
        throw new Error('Failed to upload image to S3');
      }
      return response.publicUrl
    } catch (e){
      console.log('Could not generate S3 url: ' + e)
    }
  }
  
  const onSubmit = async (data: { title: string; description: string; category: number; condition: string; price: number; }) => {
    const pubUrl = await generateS3Url();
    let postData = {
      sellerId: user.id,
      title: data.title,
      description: data.description,
      categoryId: categoriyIds[categories[data.category] as keyof typeof categoriyIds],
      condition: data.condition.toUpperCase(),
      price: data.price,
      images: JSON.stringify(pubUrl)
    }
    try{
      const submit = await postListings(postData);
      toast.success('Listing created successfully!');
    }
    catch (error) {
      console.error('Error creating listing:', error);
    }
  }

  const [file, setFile] = useState<File[]>([]);
  const fileTypes = ["JPG","JPEG", "PNG", "GIF"];

  const handleChange = (file: File | File[]) => {
    setFile(Array.isArray(file) ? file : [file]);
  };

  return (
    <div className="listing-form-container">
      <div className='listing-form'>
        <form className='lisitng-form-content' onSubmit={handleSubmit(onSubmit)}>
          <input className='listing-content'
            {...register('title', { required: "Title is required." , maxLength: 100 })}
            placeholder='Title'
          />
          <input className='listing-content'
            {...register('description', { maxLength: 100 })}
            placeholder='Description'
          />
          <select className='listing-selection' {...register('category', { required: "Category is required." })}>
            <option>Select a category</option>
            {categories.map((option, index) => (
              <option value={index}>{option}</option>
            ))}
          </select>
          <select className='listing-selection' {...register('condition', { required: "Condition is required." })}>
            <option>Select a condition</option>
            {Object.values(conditions).map((option) => (
              <option value={option}>{option}</option>
            ))}
          </select>
          <input className='listing-price'
            type="number" placeholder="Price" pattern="^\d*(\.\d{0,2})?$" step="any" {...register("price", 
              {required: true, min: 0})} 
          />
          <FileUploader
            multiple={true}
            handleChange={handleChange}
            name="file"
            types={fileTypes}
          />
          <p className='listing'>{file ? Array.isArray(file) && file.length > 1 ? `Selected multiple images`: `Selected an image` : "No files uploaded yet"}</p>
          <input className='listing-content' type="submit" />
        </form>
      </div>
      <ToastContainer />
    </div>
  );
}

export default ListingForm;