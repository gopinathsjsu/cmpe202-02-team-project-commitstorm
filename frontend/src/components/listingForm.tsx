import { useState } from 'react';
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
    "Arts & Crafts": "982b701b-accd-47d3-ab5a-a63807db2d13",
    "Automotive": "8ea454e5-a2a0-454a-93ee-2ee4cc1d0159",
    "Books": "2dfe2c43-c275-445e-aac4-db17e83fdd02",
    "Clothing": "3b28542d-ae26-4be3-8075-f48cf6437967",
    "Electronics": "2ce42fb7-e878-4499-8db7-13c8fb76a12f",
    "Electronics & Gadgets": "10ecb86a-0f87-42b8-b0bf-a5e317a7b9bb",
    "Furniture": "8f0bd49e-2a00-43e6-9d12-31915c9494b6",
    "Health & Beauty": "433db090-ff69-4363-af00-31db5155f91b",
    "Home & Garden": "304c8fec-5df0-48ba-84d8-648171d12587",
    "Musical Instruments": "3b3979e8-0368-4ce3-aad8-555017b545cd",
    "Office Supplies": "4da16f3c-00d6-40e2-bef4-4fa6735bf24c",
    "Sports": "dacbe637-1498-465a-806b-3f809d933488",
    "Toys & Games": "0a598b73-0ec3-4307-94b5-6767110ae7ab"
  }

  const [s3Urls, setS3Urls] = useState<string[]>([]);
  const generateS3Url = async () => {
    let regex = new RegExp('[^.]+$');
    let batch = false
    let data = {}
    if (file && file.length > 1){
      batch = true;
      data = file.map(f => ({contentType: `image/${f.name.match(regex)}`, fileName: f.name}));
    }
    else if (file){
      data = {contentType: `image/${file[0].name.match(regex)}`, fileName: file[0].name}
    }
    else{
      return
    }
    try{
      const urls = await getS3Url(data, batch)
      setS3Urls(urls)
    } catch (e){
      console.log('Could not generate S3 url: ' + e)
    }
  }
  
  const onSubmit = async (data: { title: string; description: string; category: number; condition: string; price: number; }) => {
    generateS3Url()
    let postData = {
      sellerId: user.id,
      title: data.title,
      description: data.description,
      category: categoriyIds[categories[data.category] as keyof typeof categoriyIds],
      condition: data.condition.toUpperCase(),
      price: data.price,
      imageUrl: s3Urls
    }
    try{
      const submit = await postListings(postData);
      toast.success('Listing created successfully!');
    }
    catch (error) {
      console.error('Error creating listing:', error);
    }
  }

  const [file, setFile] = useState<File[] | null>(null);
  const fileTypes = ["JPEG", "PNG", "GIF"];

  const handleChange = (file: File | File[]) => {
    setFile(Array.isArray(file)? file: [file]);
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
            {...register('description', { required: "Description is required." , maxLength: 100 })}
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