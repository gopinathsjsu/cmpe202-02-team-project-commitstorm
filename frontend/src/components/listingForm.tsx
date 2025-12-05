
import { useState, useMemo, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useSearchParams, useNavigate } from 'react-router';
import { FileUploader } from "react-drag-drop-files";
import '../css/listingForm.css';
import { postListings, updateListing, getListingById } from '../services/listingsService';
import { ToastContainer, toast } from 'react-toastify';
import { getS3Url } from '../services/imageService';
import SuccessModal from './SuccessModal';

const ListingForm = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const editListingId = searchParams.get('edit');
  const isEditMode = !!editListingId;
  
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('auth.user') || '{}'));
  const [loading, setLoading] = useState(isEditMode);
  const [existingListing, setExistingListing] = useState(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [formDefaults, setFormDefaults] = useState<{ title: string; description: string; category: number | string; condition: string; price: number; }>({
    title: '',
    description: '',
    category: '',
    condition: '',
    price: 0
  });
  const { register, handleSubmit, setValue, reset } = useForm<{ title: string; description: string; category: number; condition: string; price: number; }>({
    defaultValues: formDefaults
  });
  
  const categories = ["Arts & Crafts","Automotive","Books","Clothing","Electronics","Electronics & Gadgets","Furniture",
  "Health & Beauty","Home & Garden","Musical Instruments","Office Supplies","Sports","Toys & Games"];
  const conditions = {
    NEW: 'New',
    LIKE_NEW: 'Like New',
    GOOD: 'Good',
    FAIR: 'Fair',
    POOR: 'Poor'
  };
  
  // Reverse mapping for conditions
  const conditionToKey = {
    'New': 'NEW',
    'Like New': 'LIKE_NEW',
    'Good': 'GOOD',
    'Fair': 'FAIR',
    'Poor': 'POOR'
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
  
  // Reverse mapping from categoryId to category name
  const categoryIdToName = Object.fromEntries(
    Object.entries(categoriyIds).map(([name, id]) => [id, name])
  );

  // Load existing listing data when in edit mode
  useEffect(() => {
    const loadListingData = async () => {
      if (!isEditMode || !editListingId) return;
      
      setLoading(true);
      try {
        const listing = await getListingById(editListingId);
        setExistingListing(listing);
        
        // Parse images
        let imagesArray = [];
        if (listing.images) {
          try {
            if (Array.isArray(listing.images)) {
              imagesArray = listing.images;
            } else if (typeof listing.images === 'string') {
              imagesArray = JSON.parse(listing.images);
              if (!Array.isArray(imagesArray)) {
                imagesArray = [listing.images];
              }
            }
          } catch (e) {
            imagesArray = [listing.images];
          }
        }
        
        // Find category index
        const categoryName = listing.categoryName || categoryIdToName[listing.categoryId] || '';
        const categoryIndex = categories.findIndex(cat => cat === categoryName);
        
        // Find condition value
        const conditionValue = Object.keys(conditionToKey).find(
          key => conditionToKey[key] === listing.condition
        ) || listing.condition;
        
        // Set form defaults and populate form with existing data
        const defaults = {
          title: listing.title || '',
          description: listing.description || '',
          category: categoryIndex >= 0 ? categoryIndex : '',
          condition: conditionValue || '',
          price: parseFloat(listing.price) || 0
        };
        
        setFormDefaults(defaults);
        reset(defaults);
        
        // Also set values explicitly to ensure they're populated
        setValue('title', defaults.title);
        setValue('description', defaults.description);
        setValue('category', defaults.category as number);
        setValue('condition', defaults.condition);
        setValue('price', defaults.price);
        
        setLoading(false);
      } catch (error) {
        console.error('Error loading listing:', error);
        toast.error('Failed to load listing data');
        setLoading(false);
        navigate('/create-listing');
      }
    };
    
    loadListingData();
  }, [isEditMode, editListingId, setValue]);

  const generateS3Url = async () => {
    if (!file || file.length === 0) {
      return null;
    }
    
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
      return null;
    }
  }
  
  const onSubmit = async (data: { title: string; description: string; category: number; condition: string; price: number; }) => {
    try {
      // Get images - use new upload if provided, otherwise use existing
      let imagesArray = [];
      if (file && file.length > 0) {
        const pubUrl = await generateS3Url();
        if (pubUrl) {
          imagesArray = Array.isArray(pubUrl) ? pubUrl : [pubUrl];
        }
      } else if (existingListing?.images) {
        // Keep existing images when editing
        try {
          if (Array.isArray(existingListing.images)) {
            imagesArray = existingListing.images;
          } else if (typeof existingListing.images === 'string') {
            const parsed = JSON.parse(existingListing.images);
            imagesArray = Array.isArray(parsed) ? parsed : [existingListing.images];
          }
        } catch (e) {
          imagesArray = [existingListing.images];
        }
      }
      
      // Get condition - ensure it's uppercase
      let conditionValue = data.condition;
      if (conditionToKey[conditionValue as keyof typeof conditionToKey]) {
        conditionValue = conditionToKey[conditionValue as keyof typeof conditionToKey];
      }
      conditionValue = conditionValue.toUpperCase();
      
      // Get category ID
      const categoryName = categories[data.category] as keyof typeof categoriyIds;
      const categoryId = categoriyIds[categoryName];
      
      if (isEditMode && editListingId) {
        // Update existing listing - match exact API format as specified
        const updateData = {
          title: data.title,
          description: data.description,
          price: parseFloat(data.price),
          condition: conditionValue,
          status: existingListing?.status || 'ACTIVE',
          categoryId: categoryId, // Send as string
          sellerId: user?.id || existingListing?.sellerId || existingListing?.seller?.id // Get sellerId from user or listing
        };
        
        console.log('Updating listing with data:', updateData);
        console.log('API endpoint:', `PUT /api/listings/${editListingId}`);
        
        await updateListing(editListingId, updateData);
        setShowSuccessModal(true);
      } else {
        // Create new listing
        const postData = {
          sellerId: user.id,
          title: data.title,
          description: data.description,
          categoryId: categoryId,
          condition: conditionValue,
          price: parseFloat(data.price),
          images: JSON.stringify(imagesArray.length > 0 ? imagesArray : [])
        };
        
        await postListings(postData);
        toast.success('Listing created successfully!');
        navigate('/marketplace');
      }
    } catch (error: any) {
      console.error(`Error ${isEditMode ? 'updating' : 'creating'} listing:`, error);
      toast.error(error.message || `Failed to ${isEditMode ? 'update' : 'create'} listing`);
    }
  }

  const [file, setFile] = useState<File[]>([]);
  const fileTypes = ["JPG","JPEG", "PNG", "GIF"];

  const handleChange = (file: File | File[]) => {
    setFile(Array.isArray(file) ? file : [file]);
  };

  if (loading) {
    return (
      <div className="listing-form-container">
        <div className='listing-form'>
          <p>Loading listing data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="listing-form-container">
      <div className='listing-form'>
        <h2 style={{ marginBottom: '20px', color: 'white' }}>{isEditMode ? 'Edit Listing' : 'Create New Listing'}</h2>
        <form className='lisitng-form-content' onSubmit={handleSubmit(onSubmit)}>
          <input className='listing-content'
            {...register('title', { required: "Title is required." , maxLength: 100 })}
            placeholder='Title'
          />
          <textarea 
            className='listing-content'
            {...register('description', { maxLength: 500 })}
            placeholder='Description'
            rows={4}
            style={{ resize: 'vertical', minHeight: '80px' }}
          />
          <select className='listing-selection' {...register('category', { required: "Category is required." })}>
            <option value="">Select a category</option>
            {categories.map((option, index) => (
              <option key={index} value={index}>{option}</option>
            ))}
          </select>
          <select className='listing-selection' {...register('condition', { required: "Condition is required." })}>
            <option value="">Select a condition</option>
            {Object.values(conditions).map((option) => (
              <option key={option} value={option}>{option}</option>
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
          <p className='listing'>
            {file && file.length > 0 
              ? (Array.isArray(file) && file.length > 1 ? `Selected ${file.length} images` : `Selected an image`) 
              : (isEditMode && existingListing?.images ? "Using existing images (upload new to replace)" : "No files uploaded yet")}
          </p>
          <input className='listing-content' type="submit" value={isEditMode ? 'Update Listing' : 'Create Listing'} />
        </form>
      </div>
      <ToastContainer />
      
      {/* Success Modal for Edit */}
      {showSuccessModal && (
        <SuccessModal
          message="Listing updated successfully!"
          onClose={() => {
            setShowSuccessModal(false);
            navigate('/marketplace');
          }}
        />
      )}
    </div>
  );
}

export default ListingForm;