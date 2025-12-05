import { useState, useEffect } from 'react';
import { getListingById, updateListing } from '../services/listingsService';
import Loader from './Loader';
import SuccessModal from './SuccessModal';

const EditListingModal = ({ listingId, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [listing, setListing] = useState(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: 0,
    condition: '',
    category: '',
  });

  const categories = ["Arts & Crafts","Automotive","Books","Clothing","Electronics","Electronics & Gadgets","Furniture",
  "Health & Beauty","Home & Garden","Musical Instruments","Office Supplies","Sports","Toys & Games"];

  const conditions = {
    'NEW': 'New',
    'LIKE_NEW': 'Like New',
    'GOOD': 'Good',
    'FAIR': 'Fair',
    'POOR': 'Poor'
  };

  const conditionOptions = Object.entries(conditions).map(([key, value]) => ({ key, value }));

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
  };

  const categoryIdToName = Object.fromEntries(
    Object.entries(categoriyIds).map(([name, id]) => [id, name])
  );

  // Load listing data
  useEffect(() => {
    const loadListing = async () => {
      if (!listingId) return;
      
      setLoading(true);
      setError('');
      try {
        const listingData = await getListingById(listingId);
        setListing(listingData);
        
        // Parse images
        let imagesArray = [];
        if (listingData.images) {
          try {
            if (Array.isArray(listingData.images)) {
              imagesArray = listingData.images;
            } else if (typeof listingData.images === 'string') {
              const parsed = JSON.parse(listingData.images);
              imagesArray = Array.isArray(parsed) ? parsed : [listingData.images];
            }
          } catch (e) {
            imagesArray = typeof listingData.images === 'string' ? [listingData.images] : [];
          }
        }

        // Find category name
        const categoryName = listingData.categoryName || categoryIdToName[listingData.categoryId] || '';
        
        // Set form data
        setFormData({
          title: listingData.title || '',
          description: listingData.description || '',
          price: parseFloat(listingData.price) || 0,
          condition: listingData.condition || 'NEW',
          category: categoryName || '',
        });
        
        setLoading(false);
      } catch (err) {
        console.error('Error loading listing:', err);
        setError(err.message || 'Failed to load listing data');
        setLoading(false);
      }
    };

    loadListing();
  }, [listingId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'price' ? parseFloat(value) || 0 : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title || !formData.category || !formData.condition) {
      setError('Please fill in all required fields');
      return;
    }

    setSaving(true);
    setError('');

    try {
      // Get category ID
      const categoryId = categoriyIds[formData.category];
      if (!categoryId) {
        throw new Error('Invalid category selected');
      }

      // Get sellerId from listing or current user
      const userData = localStorage.getItem('auth.user');
      const currentUser = userData ? JSON.parse(userData) : null;
      const sellerId = listing?.sellerId || listing?.seller?.id || currentUser?.id;

      // Prepare update data - match exact API format
      const updateData = {
        title: formData.title,
        description: formData.description,
        price: parseFloat(formData.price),
        condition: formData.condition, // Already uppercase (NEW, LIKE_NEW, etc.)
        status: listing?.status || 'ACTIVE',
        categoryId: categoryId, // Send as string
        sellerId: sellerId // Get sellerId from listing or current user
      };

      console.log('Updating listing with data:', updateData);
      console.log('API endpoint:', `PUT /api/listings/${listingId}`);

      await updateListing(listingId, updateData);
      
      // Trigger refresh immediately after successful edit
      if (onSuccess) {
        onSuccess();
      }
      
      // Close edit modal and show success modal
      setSaving(false);
      onClose();
      setShowSuccessModal(true);
    } catch (err) {
      console.error('Error updating listing:', err);
      setError(err.message || 'Failed to update listing. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  // Handle ESC key
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && !saving) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [onClose, saving]);

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl p-6">
          <Loader text="Loading listing data..." />
        </div>
      </div>
    );
  }

  return (
    <>
    <div 
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      onClick={(e) => {
        if (e.target === e.currentTarget && !saving) {
          onClose();
        }
      }}
    >
      <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center p-6 border-b border-gray-700">
          <h2 className="text-2xl font-bold text-white">Edit Listing</h2>
          <button
            onClick={onClose}
            disabled={saving}
            className="text-gray-400 hover:text-white text-2xl font-bold disabled:opacity-50"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="bg-red-900/50 border border-red-500 text-red-200 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Title <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
              disabled={saving}
              maxLength={100}
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              rows={4}
              disabled={saving}
              maxLength={500}
            />
          </div>

          {/* Price */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Price <span className="text-red-400">*</span>
            </label>
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleChange}
              className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
              disabled={saving}
              min="0"
              step="0.01"
            />
          </div>

          {/* Condition */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Condition <span className="text-red-400">*</span>
            </label>
            <select
              name="condition"
              value={formData.condition}
              onChange={handleChange}
              className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
              disabled={saving}
            >
              {conditionOptions.map(({ key, value }) => (
                <option key={key} value={key}>{value}</option>
              ))}
            </select>
          </div>

          {/* Category */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Category <span className="text-red-400">*</span>
            </label>
            <select
              name="category"
              value={formData.category}
              onChange={handleChange}
              className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
              disabled={saving}
            >
              <option value="">Select a category</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          {/* Images info */}
          <div className="text-sm text-gray-400">
            <p>Images: Using existing images. Image upload not available in edit mode.</p>
          </div>

          {/* Buttons */}
          <div className="flex gap-3 pt-4">
            <button
              type="submit"
              disabled={saving}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="flex-1 bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg font-medium disabled:opacity-50"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
    
    {/* Success Modal */}
    {showSuccessModal && (
      <SuccessModal
        message="Listing updated successfully!"
        onClose={() => {
          setShowSuccessModal(false);
        }}
      />
    )}
    </>
  );
};

export default EditListingModal;

