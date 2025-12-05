import { useState, useEffect } from 'react';
import { deleteListing } from '../services/listingsService';

const DeleteListingModal = ({ listingId, listingTitle, onClose, onSuccess }) => {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  const handleConfirm = async () => {
    if (!listingId) {
      setError('Listing ID is missing');
      return;
    }

    setDeleting(true);
    setError('');

    try {
      console.log('Deleting listing:', listingId);
      console.log('API endpoint:', `DELETE /api/listings/${listingId}`);
      
      await deleteListing(listingId);
      
      // Trigger refresh immediately after successful delete
      if (onSuccess) {
        onSuccess();
      }
      
      // Show success message
      alert('Listing deleted successfully!');
      
      onClose();
    } catch (err) {
      console.error('Error deleting listing:', err);
      setError(err.message || 'Failed to delete listing. Please try again.');
      setDeleting(false);
    }
  };

  // Handle ESC key
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && !deleting) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [onClose, deleting]);

  return (
    <div 
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      onClick={(e) => {
        if (e.target === e.currentTarget && !deleting) {
          onClose();
        }
      }}
    >
      <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-md">
        <div className="flex justify-between items-center p-6 border-b border-gray-700">
          <h2 className="text-2xl font-bold text-white">Delete Listing</h2>
          <button
            onClick={onClose}
            disabled={deleting}
            className="text-gray-400 hover:text-white text-2xl font-bold disabled:opacity-50"
          >
            ×
          </button>
        </div>

        <div className="p-6">
          {error && (
            <div className="bg-red-900/50 border border-red-500 text-red-200 px-4 py-3 rounded-lg mb-4">
              {error}
            </div>
          )}

          <p className="text-gray-300 mb-6">
            Are you sure you want to delete <span className="font-semibold text-white">"{listingTitle || 'this listing'}"</span>? 
            This action cannot be undone.
          </p>

          <div className="flex gap-3">
            <button
              onClick={handleConfirm}
              disabled={deleting}
              className="flex-1 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {deleting ? 'Deleting...' : 'Confirm'}
            </button>
            <button
              onClick={onClose}
              disabled={deleting}
              className="flex-1 bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg font-medium disabled:opacity-50"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeleteListingModal;

